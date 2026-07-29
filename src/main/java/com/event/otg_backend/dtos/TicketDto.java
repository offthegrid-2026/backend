package com.event.otg_backend.dtos;

import com.event.otg_backend.models.User;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDto {
    private int id;
    private UserDto user;
    private Double price;
    private Instant timestamp;
    private String ticketCode;
}
