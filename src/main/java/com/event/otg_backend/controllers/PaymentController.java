package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.payment.CreateOrderRequestDto;
import com.event.otg_backend.dtos.payment.CreateOrderResponseDto;
import com.event.otg_backend.dtos.payment.PaymentVerificationDto;
import com.event.otg_backend.dtos.payment.VerifyPaymentResponseDto;
import com.event.otg_backend.helpers.security.CurrentUserProvider;
import com.event.otg_backend.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto request){
        Long userId = CurrentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(paymentService.createOrder(userId, request.getTicketType()));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponseDto> verify(@Valid @RequestBody PaymentVerificationDto request){
        Long userId = CurrentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(paymentService.verifyPayment(userId, request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader (value = "X-Razorpay-Signature", required = false)
            String signature){

        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }

}
