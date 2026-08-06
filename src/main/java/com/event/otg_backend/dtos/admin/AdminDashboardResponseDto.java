package com.event.otg_backend.dtos.admin;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardResponseDto {

    private long totalRegistrations;
    private List<TicketTypeSalesDto> ticketSales;
    private double totalSales;
}
