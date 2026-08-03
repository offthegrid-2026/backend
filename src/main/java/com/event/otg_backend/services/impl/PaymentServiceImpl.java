package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.CreateOrderResponseDto;
import com.event.otg_backend.dtos.PaymentVerificationDto;
import com.event.otg_backend.dtos.VerifyPaymentResponseDto;
import com.event.otg_backend.events.TicketConfirmedEvent;
import com.event.otg_backend.exceptions.*;
import com.event.otg_backend.helpers.ProfileCompletionChecker;
import com.event.otg_backend.models.*;
import com.event.otg_backend.repository.PaymentRepository;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.repository.TicketTypeRepository;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.PaymentService;
import com.event.otg_backend.services.TicketService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final RazorpayClient razorpayClient;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${spring.app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${spring.app.payment.currency}")
    private String currency;

    @Value("${spring.app.payment.reservation-hold-minutes}")
    private long holdMinutes;

    @Value("${spring.app.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;


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

        Instant now = Instant.now();
        Instant holdThreshold = now.minus(Duration.ofMinutes(holdMinutes));

        // Gate 3: ONE active hold per user. If a pending order already exists, return it
        // instead of creating a second one -> prevents double-charge and seat-hold spam.

        Optional<Payment> existingHold = paymentRepository.findFirstByUserAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(user, PaymentStatus.CREATED, holdThreshold);

        if (existingHold.isPresent()){
            log.debug("Reusing active hold for userId={}, orderId={}", userId, existingHold.get().getRazorpayOrderId());
            return toResponse(existingHold.get());
        }

        // Lock this ticket type's row -> serializes seat counting + hold creation (no oversell)
        TicketType type = ticketTypeRepository.findByCodeForUpdate(ticketTypeCode).orElseThrow(()-> new IllegalArgumentException("Invalid ticket type."));


        // Gate 4: sale window (server-side)
        if (now.isBefore(type.getSaleStartAt()) || now.isAfter(type.getSaleEndAt())){
            throw new TicketSaleClosedException("This ticket is not on sale right now.");
        }

        // Gate 5: seats available? committed = paid + active (non-expired) holds
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

        log.info("Order created: orderId={}, userId={}, ticketType={}, amountPaise={}",
                orderId, userId, ticketTypeCode, payment.getAmountPaise());

        return toResponse(payment);
    }

    private CreateOrderResponseDto toResponse(Payment payment) {

        return CreateOrderResponseDto.builder()
                .razorpayOrderId(payment.getRazorpayOrderId())
                .amountPaise(payment.getAmountPaise())
                .currency(payment.getCurrency())
                .razorpayKeyId(razorpayKeyId)
                .ticketType(payment.getTicketTypeCode())
                .build();
    }

    @Override
    @Transactional
    public VerifyPaymentResponseDto verifyPayment(Long userId, PaymentVerificationDto dto) {

        if(!isSignatureValid(dto)){
            log.warn("Payment signature verification failed: orderId={}, userId={}", dto.getRazorpayOrderId(), userId);
            throw new PaymentVerificationException("Payment Verification Failed.");
        }

        Ticket ticket = confirmPaidOrder(dto.getRazorpayOrderId(), dto.getRazorpayPaymentId(), userId);

        return VerifyPaymentResponseDto.builder()
                .success(true)
                .ticketCode(ticket.getTicketCode())
                .message("Payment Successful. Your ticket is confirmed.")
                .build();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {

        // 1. Verify the signature over the RAW body
        if (signature == null) {
            log.warn("Webhook rejected: missing X-Razorpay-Signature header");
            throw new PaymentVerificationException("Missing webhook signature.");
        }
        boolean valid;
        try {
            valid = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
        }catch (RazorpayException e){
            log.error("Webhook signature verification threw an error: {}", e.getMessage(), e);
            valid = false;
        }
        if(!valid){
            log.warn("Webhook rejected: invalid signature");
            throw new PaymentVerificationException("Invalid webhook signature.");
        }

        // 2. We only act on a captured payment.
        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event");
        log.info("Webhook received: event={}", eventType);
        if(!"payment.captured".equals(eventType)){
            return; // ack and ignore everything else
        }

        JSONObject entity = event
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String orderId = entity.optString("order_id", null);
        String paymentId = entity.optString("id", null);

        if(orderId == null || paymentId == null){
            log.warn("Webhook payment.captured missing order_id or payment_id — ignored");
            return;
        }

        try{
            confirmPaidOrder(orderId, paymentId, null);
        }catch (ResourceNotFoundException e){
            log.warn("Webhook for unknown order {} — no matching payment record, ignored", orderId);
        }
    }

    private Ticket confirmPaidOrder(String orderId, String paymentId, Long expectedUserId) {

        Payment payment = paymentRepository.findByRazorpayOrderIdForUpdate(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if(expectedUserId != null && !payment.getUser().getId().equals(expectedUserId)){
            log.warn("Ownership mismatch: orderId={} belongs to userId={}, but userId={} tried to confirm it",
                    orderId, payment.getUser().getId(), expectedUserId);
            throw new PaymentVerificationException("This order doesn't belong to you.");
        }

        if (payment.getStatus() == PaymentStatus.PAID){
            log.debug("Order {} already PAID — returning existing ticket (idempotent)", orderId);
            return ticketService.generateTicketForUser(payment.getUser(), payment.getTicketTypeCode(), payment.getAmountPaise());
        }

        User user = payment.getUser();

        // DUPLICATE-PAYMENT GUARD: this order is still CREATED, but the user ALREADY has a ticket
        // from a different order (e.g. an expired hold that got paid late). Never charge twice for one
        // ticket -> refund this payment instead of confirming it again.
        Optional<Ticket> existingTicket = ticketRepository.findByUser(user);
        if (existingTicket.isPresent()) {
            log.warn("Duplicate payment: userId={} already has a ticket; second order {} was paid (paymentId={}). Refunding.",
                    user.getId(), orderId, paymentId);
            payment.setRazorpayPaymentId(paymentId);
            boolean refunded = refundPayment(paymentId);
            payment.setStatus(refunded ? PaymentStatus.REFUNDED : PaymentStatus.REFUND_FAILED);
            paymentRepository.save(payment);
            return existingTicket.get();   // user already has their ticket; the extra charge is refunded
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        Ticket ticket = ticketService.generateTicketForUser(user, payment.getTicketTypeCode(), payment.getAmountPaise());

        log.info("Payment confirmed: orderId={}, paymentId={}, userId={}, ticketCode={}",
                orderId, paymentId, user.getId(), ticket.getTicketCode());

        eventPublisher.publishEvent(new TicketConfirmedEvent(ticket.getId()));

        return ticket;
    }

    // Full refund of a duplicate payment via Razorpay. Returns true on success. On failure it logs loudly
    // and the caller marks the payment REFUND_FAILED so it can be found and refunded manually. Never
    // rethrows — the user still holds a valid ticket, so a refund hiccup must not break confirmation.
    private boolean refundPayment(String paymentId) {
        try {
            JSONObject request = new JSONObject();
            request.put("speed", "normal");
            razorpayClient.payments.refund(paymentId, request);
            log.info("Refund initiated for duplicate paymentId={}", paymentId);
            return true;
        } catch (RazorpayException e) {
            log.error("MANUAL REFUND REQUIRED — auto-refund failed for duplicate paymentId={}: {}",
                    paymentId, e.getMessage(), e);
            return false;
        }
    }

    private boolean isSignatureValid(PaymentVerificationDto dto) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", dto.getRazorpayOrderId());
            options.put("razorpay_payment_id", dto.getRazorpayPaymentId());
            options.put("razorpay_signature", dto.getRazorpaySignature());
            return Utils.verifyPaymentSignature(options, razorpayKeySecret);
        }catch (RazorpayException e){
            log.error("Error verifying payment signature for orderId={}: {}", dto.getRazorpayOrderId(), e.getMessage(), e);
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
            log.error("Razorpay order creation failed for userId={}, ticketType={}: {}",
                    user.getId(), type.getCode(), e.getMessage(), e);
            throw new PaymentException("Could not initiate payment. Please try again.");
        }
    }
}
