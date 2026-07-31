package com.event.otg_backend.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderResponseDto {

    private String razorpayOrderId;
    private Long amountPaise;
    private String currency;
    private String razorpayKeyId;
    private String ticketType;
}
