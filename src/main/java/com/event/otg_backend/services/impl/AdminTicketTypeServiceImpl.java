package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.admin.TicketTypeConfigDto;
import com.event.otg_backend.dtos.admin.TicketTypeUpdateDto;
import com.event.otg_backend.exceptions.ResourceNotFoundException;
import com.event.otg_backend.helpers.security.CurrentStaffProvider;
import com.event.otg_backend.models.PaymentStatus;
import com.event.otg_backend.models.TicketType;
import com.event.otg_backend.repository.PaymentRepository;
import com.event.otg_backend.repository.TicketTypeRepository;
import com.event.otg_backend.services.AdminTicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTicketTypeServiceImpl implements AdminTicketTypeService {

    /** Sale dates are entered and displayed in Indian time; storage is always UTC. */
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentRepository paymentRepository;

    @Value("${spring.app.payment.reservation-hold-minutes}")
    private long holdMinutes;


    @Override
    @Transactional(readOnly = true)
    public List<TicketTypeConfigDto> listTicketTypes() {
        return ticketTypeRepository.findAll(Sort.by("id")).stream()
                .map(this::toDto)
                .toList();
    }


    @Override
    @Transactional
    public TicketTypeConfigDto updateTicketType(String code, TicketTypeUpdateDto dto) {

        // Same lock the order path takes (PaymentServiceImpl.createOrder) so an admin edit
        // can never interleave with a seat count.
        TicketType type = ticketTypeRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown ticket type: " + code));

        String before = describe(type);

        if (dto.getDisplayName() != null) {
            type.setDisplayName(dto.getDisplayName().trim());
        }
        if (dto.getPriceRupees() != null) {
            type.setPricePaise(toPaise(dto.getPriceRupees()));
        }
        if (dto.getSeatLimit() != null) {
            type.setSeatLimit(dto.getSeatLimit());
        }
        if (dto.getSaleStartDate() != null) {
            // Sales open at the very start of that day, IST.
            type.setSaleStartAt(dto.getSaleStartDate().atStartOfDay(IST).toInstant());
        }
        if (dto.getSaleEndDate() != null) {
            // Sales close at the END of that day, IST - an end date of the 5th must
            // include the whole of the 5th, not stop at midnight as it begins.
            type.setSaleEndAt(dto.getSaleEndDate().atTime(LocalTime.MAX).atZone(IST).toInstant());
        }

        // Validate the merged result, not just the incoming fields - changing only one
        // end of the window can still invert it.
        if (!type.getSaleStartAt().isBefore(type.getSaleEndAt())) {
            throw new IllegalArgumentException("Sale start must be before sale end.");
        }

        TicketType saved = ticketTypeRepository.save(type);

        log.info("Ticket type {} updated by {}: [{}] -> [{}]",
                code, CurrentStaffProvider.getCurrentStaffEmail(), before, describe(saved));

        return toDto(saved);
    }


    /** Rupees -> paise. Exact by construction: @Digits caps input at 2 decimals. */
    private long toPaise(BigDecimal rupees) {
        return rupees.movePointRight(2).longValueExact();
    }

    private BigDecimal toRupees(long paise) {
        return BigDecimal.valueOf(paise).movePointLeft(2);
    }

    private LocalDate toIstDate(Instant instant) {
        return instant.atZone(IST).toLocalDate();
    }


    private TicketTypeConfigDto toDto(TicketType type) {

        Instant holdThreshold = Instant.now().minus(Duration.ofMinutes(holdMinutes));

        long sold = paymentRepository.countByTicketTypeCodeAndStatus(type.getCode(), PaymentStatus.PAID);
        long activeHolds = paymentRepository.countByTicketTypeCodeAndStatusAndCreatedAtAfter(
                type.getCode(), PaymentStatus.CREATED, holdThreshold);

        return TicketTypeConfigDto.builder()
                .code(type.getCode())
                .displayName(type.getDisplayName())
                .priceRupees(toRupees(type.getPricePaise()))
                .pricePaise(type.getPricePaise())
                .seatLimit(type.getSeatLimit())
                .saleStartDate(toIstDate(type.getSaleStartAt()))
                .saleEndDate(toIstDate(type.getSaleEndAt()))
                .saleStartAt(type.getSaleStartAt())
                .saleEndAt(type.getSaleEndAt())
                .sold(sold)
                .activeHolds(activeHolds)
                .remaining(Math.max(0, type.getSeatLimit() - sold - activeHolds))
                .build();
    }

    private String describe(TicketType t) {
        return "price=" + t.getPricePaise() + "p, seats=" + t.getSeatLimit()
                + ", window=" + t.getSaleStartAt() + ".." + t.getSaleEndAt();
    }
}
