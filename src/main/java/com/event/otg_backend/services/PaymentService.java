package com.event.otg_backend.services;

import com.event.otg_backend.dtos.payment.CreateOrderResponseDto;
import com.event.otg_backend.dtos.payment.PaymentVerificationDto;
import com.event.otg_backend.dtos.payment.VerifyPaymentResponseDto;

public interface PaymentService {

    CreateOrderResponseDto createOrder(Long userId, String ticketTypeCode);

    VerifyPaymentResponseDto verifyPayment(Long userId, PaymentVerificationDto dto);

    void handleWebhook(String payload, String signature);

}
