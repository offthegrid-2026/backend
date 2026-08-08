package com.event.otg_backend.dtos.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketTypeConfigDto {

    private String code;
    private String displayName;

    private BigDecimal priceRupees;   // for display / form prefill
    private Long pricePaise;          // what is actually stored

    private Integer seatLimit;

    private LocalDate saleStartDate;  // IST dates, for the form
    private LocalDate saleEndDate;
    private Instant saleStartAt;      // the stored UTC instants, so the conversion is auditable
    private Instant saleEndAt;

    private long sold;                // PAID payments
    private long activeHolds;         // unexpired CREATED payments
    private long remaining;
}
