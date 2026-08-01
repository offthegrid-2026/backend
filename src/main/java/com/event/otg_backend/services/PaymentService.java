package com.event.otg_backend.services;

import com.event.otg_backend.dtos.CreateOrderResponseDto;
import com.event.otg_backend.dtos.PaymentVerificationDto;
import com.event.otg_backend.dtos.VerifyPaymentResponseDto;

public interface PaymentService {

    CreateOrderResponseDto createOrder(Long userId, String ticketTypeCode);

    VerifyPaymentResponseDto verifyPayment(Long userId, PaymentVerificationDto dto);

    void handleWebhook(String payload, String signature);

}
