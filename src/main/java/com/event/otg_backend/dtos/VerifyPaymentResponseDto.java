package com.event.otg_backend.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentResponseDto {
    private boolean success;
    private String ticketCode;
    private String message;
}
