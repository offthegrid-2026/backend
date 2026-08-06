package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.AdminDashboardResponseDto;
import com.event.otg_backend.dtos.TicketTypeSalesDto;
import com.event.otg_backend.models.TicketType;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.repository.TicketTypeRepository;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public AdminDashboardResponseDto getDashboard() {

        long totalRegistrations = userRepository.count();

        List<TicketTypeSalesDto> ticketSales = ticketTypeRepository.findAll().stream().map(this::toSalesDto).toList();

        double totalSales = ticketRepository.sumAllPrices();

        return AdminDashboardResponseDto.builder()
                .totalRegistrations(totalRegistrations)
                .ticketSales(ticketSales)
                .totalSales(totalSales)
                .build();
    }

    private TicketTypeSalesDto toSalesDto(TicketType type) {

        long sold = ticketRepository.countByTicketTypeCode(type.getCode());

        long remaining = Math.max(0, type.getSeatLimit() - sold);

        return TicketTypeSalesDto.builder()
                .ticketType(type.getCode())
                .displayName(type.getDisplayName())
                .sold(sold)
                .remaining(remaining)
                .seatLimit(type.getSeatLimit())
                .build();

    }

}
