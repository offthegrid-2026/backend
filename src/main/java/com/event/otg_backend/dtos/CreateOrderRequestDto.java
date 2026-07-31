package com.event.otg_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {
    @NotBlank(message = "Ticket type is required")
    private String ticketType;  // "EARLY_BIRD" or "NORMAL"
}
