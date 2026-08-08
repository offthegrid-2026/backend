package com.event.otg_backend.dtos.ticket;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketTypePublicDto {

    private String code;
    private String displayName;
    private BigDecimal priceRupees;
    private TicketTypeStatus status;
}
