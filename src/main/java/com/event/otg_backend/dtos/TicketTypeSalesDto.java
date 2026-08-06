package com.event.otg_backend.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketTypeSalesDto {

    private String ticketType;
    private String displayName;
    private long sold;
    private long remaining;
    private int seatLimit;
}
