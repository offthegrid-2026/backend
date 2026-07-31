package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.CreateOrderRequestDto;
import com.event.otg_backend.dtos.CreateOrderResponseDto;
import com.event.otg_backend.helpers.CurrentUserProvider;
import com.event.otg_backend.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
