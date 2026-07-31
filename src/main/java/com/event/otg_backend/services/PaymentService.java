package com.event.otg_backend.services;

import com.event.otg_backend.dtos.CreateOrderResponseDto;

public interface PaymentService {

    CreateOrderResponseDto createOrder(Long userId, String ticketTypeCode);

}
