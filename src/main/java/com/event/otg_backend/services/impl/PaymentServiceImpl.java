package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.CreateOrderResponseDto;
import com.event.otg_backend.dtos.PaymentVerificationDto;
import com.event.otg_backend.dtos.VerifyPaymentResponseDto;
import com.event.otg_backend.exceptions.*;
import com.event.otg_backend.helpers.ProfileCompletionChecker;
import com.event.otg_backend.models.*;
import com.event.otg_backend.repository.PaymentRepository;
import com.event.otg_backend.repository.TicketTypeRepository;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.PaymentService;
import com.event.otg_backend.services.TicketService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;
    private final RazorpayClient razorpayClient;

    @Value("${spring.app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${spring.app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${spring.app.payment.currency}")
    private String currency;

    @Value("${spring.app.payment.reservation-hold-minutes}")
    private long holdMinutes;


    @Override
    @Transactional
    public CreateOrderResponseDto createOrder(Long userId, String ticketTypeCode) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Gate 1: check if the profile is complete
        if(!ProfileCompletionChecker.isComplete(user)){
            throw new ProfileIncompleteException("Please complete your profile before buying a ticket.");
        }

        // Gate 2: one ticket per user
        if(Boolean.TRUE.equals(user.getPaymentStatus())){
            throw new AlreadyPaidException("You have already purchased a ticket.");
        }

        // Lock this ticket type's row -> serializes seat counting + hold creation (no oversell)
        TicketType type = ticketTypeRepository.findByCodeForUpdate(ticketTypeCode).orElseThrow(() -> new IllegalArgumentException("Invalid ticket type."));

        // Gate 3: sale window (server-side)
        Instant now = Instant.now();
        if (now.isBefore(type.getSaleStartAt()) || now.isAfter(type.getSaleEndAt())){
            throw new TicketSaleClosedException("This ticket is not on sale right now.");
        }

        // Gate 4: seats available? committed = paid + active (non-expired) holds
        Instant holdThreshold = now.minus(Duration.ofMinutes(holdMinutes));
        long paid = paymentRepository.countByTicketTypeCodeAndStatus(ticketTypeCode, PaymentStatus.PAID);
        long activeHolds = paymentRepository.countByTicketTypeCodeAndStatusAndCreatedAtAfter(ticketTypeCode, PaymentStatus.CREATED, holdThreshold);

        if (paid + activeHolds >= type.getSeatLimit()){
            throw new TicketSoldOutException("Sorry, this ticket type is sold out!!");
        }

        Order razorpayOrder = createRazorpayOrder(type, user);
        String orderId = razorpayOrder.get("id");

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setTicketTypeCode(ticketTypeCode);
        payment.setRazorpayOrderId(orderId);
        payment.setAmountPaise(type.getPricePaise());
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.CREATED);
        paymentRepository.save(payment);

        return CreateOrderResponseDto.builder()
                .razorpayOrderId(orderId)
                .amountPaise(type.getPricePaise())
                .currency(currency)
                .razorpayKeyId(razorpayKeyId)
                .ticketType(ticketTypeCode)
                .build();
    }

    @Override
    @Transactional
    public VerifyPaymentResponseDto verifyPayment(Long userId, PaymentVerificationDto dto) {

        if(!isSignatureValid(dto)){
            throw new PaymentVerificationException("Payment Verification Failed.");
        }

        Ticket ticket = confirmPaidOrder(dto.getRazorpayOrderId(), dto.getRazorpayPaymentId(), userId);

        return VerifyPaymentResponseDto.builder()
                .success(true)
                .ticketCode(ticket.getTicketCode())
                .message("Payment Successful. Your ticket is confirmed.")
                .build();
    }

    private Ticket confirmPaidOrder(String orderId, String paymentId, Long expectedUserId) {

        Payment payment = paymentRepository.findByRazorpayOrderIdForUpdate(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if(expectedUserId != null && !payment.getUser().getId().equals(expectedUserId)){
            throw new PaymentVerificationException("This order doesn't belong to you.");
        }

        if (payment.getStatus() == PaymentStatus.PAID){
            return ticketService.generateTicketForUser(payment.getUser(), payment.getTicketTypeCode(), payment.getAmountPaise());
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        return ticketService.generateTicketForUser(payment.getUser(), payment.getTicketTypeCode(), payment.getAmountPaise());

    }

    private boolean isSignatureValid(PaymentVerificationDto dto) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", dto.getRazorpayOrderId());
            options.put("razorpay_payment_id", dto.getRazorpayPaymentId());
            options.put("razorpay_signature", dto.getRazorpaySignature());
            return Utils.verifyPaymentSignature(options, razorpayKeySecret);
        }catch (RazorpayException e){
            return false;
        }
    }

    private Order createRazorpayOrder(TicketType type, User user) {
        try {
            JSONObject request = new JSONObject();
            request.put("amount", type.getPricePaise());
            request.put("currency", currency);
            request.put("receipt", "rcpt_" + user.getId() + "_" + System.currentTimeMillis());

            JSONObject notes = new JSONObject();
            notes.put("userId", String.valueOf(user.getId()));
            notes.put("ticketType", type.getCode());
            request.put("notes", notes);

            return razorpayClient.orders.create(request);
        }catch (RazorpayException e){
            throw new PaymentException("Could not initiate payment. Please try again.");
        }
    }
}
