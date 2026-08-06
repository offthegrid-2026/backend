package com.event.otg_backend.dtos.ticket;

import com.event.otg_backend.dtos.user.UserDto;
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
