package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.ticket.TicketTypePublicDto;
import com.event.otg_backend.dtos.ticket.TicketTypeStatus;
import com.event.otg_backend.models.PaymentStatus;
import com.event.otg_backend.models.TicketType;
import com.event.otg_backend.repository.PaymentRepository;
import com.event.otg_backend.repository.TicketTypeRepository;
import com.event.otg_backend.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentRepository paymentRepository;

    @Value("${spring.app.payment.reservation-hold-minutes}")
    private long holdMinutes;

    @Override
    @Transactional(readOnly = true)
    public List<TicketTypePublicDto> listPurchasableTicketTypes() {
        return ticketTypeRepository.findAll(Sort.by("id")).stream()
                .map(this::toDto)
                .toList();
    }

    private TicketTypePublicDto toDto(TicketType type) {

        Instant now = Instant.now();
        Instant holdThreshold = now.minus(Duration.ofMinutes(holdMinutes));

        // Same math PaymentServiceImpl.createOrder uses to gate a real purchase,
        // so the store can never show ON_SALE for something an order would reject.
        long paid = paymentRepository.countByTicketTypeCodeAndStatus(type.getCode(), PaymentStatus.PAID);
        long activeHolds = paymentRepository.countByTicketTypeCodeAndStatusAndCreatedAtAfter(
                type.getCode(), PaymentStatus.CREATED, holdThreshold);
        long remaining = Math.max(0, type.getSeatLimit() - paid - activeHolds);

        TicketTypeStatus status;
        if (now.isBefore(type.getSaleStartAt())) {
            status = TicketTypeStatus.COMING_SOON;
        } else if (now.isAfter(type.getSaleEndAt()) || remaining <= 0) {
            status = TicketTypeStatus.SOLD_OUT;
        } else {
            status = TicketTypeStatus.ON_SALE;
        }

        return TicketTypePublicDto.builder()
                .code(type.getCode())
                .displayName(type.getDisplayName())
                .priceRupees(BigDecimal.valueOf(type.getPricePaise()).movePointLeft(2))
                .status(status)
                .build();
    }
}
