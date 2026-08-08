package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.scan.ScanRequestDto;
import com.event.otg_backend.dtos.scan.ScanResponseDto;
import com.event.otg_backend.dtos.scan.ScanStatus;
import com.event.otg_backend.helpers.security.CurrentStaffProvider;
import com.event.otg_backend.helpers.user.UserNameFormatter;
import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.services.TicketScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketScanServiceImpl implements TicketScanService {

    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public ScanResponseDto lookup(ScanRequestDto request) {

        Ticket ticket = resolve(request, false).orElse(null);
        if (ticket == null) {
            return ScanResponseDto.notFound();
        }

        ScanStatus status = (ticket.getScannedAt() == null) ? ScanStatus.VALID : ScanStatus.ALREADY_SCANNED;
        return toResponse(ticket, status);
    }

    @Override
    @Transactional
    public ScanResponseDto checkIn(ScanRequestDto request) {

        // Row-locked: two phones scanning the same ticket at once must not both admit.
        Ticket ticket = resolve(request, true).orElse(null);
        if (ticket == null) {
            return ScanResponseDto.notFound();
        }

        if (ticket.getScannedAt() != null) {
            log.info("Check-in refused, already scanned: ticketId={}, at={}, by={}",
                    ticket.getId(), ticket.getScannedAt(), ticket.getScannedBy());
            return toResponse(ticket, ScanStatus.ALREADY_SCANNED);
        }

        ticket.setScannedAt(Instant.now());
        ticket.setScannedBy(CurrentStaffProvider.getCurrentStaffEmail());
        ticketRepository.save(ticket);

        log.info("Ticket checked in: ticketId={}, userId={}, by={}",
                ticket.getId(), ticket.getUser().getId(), ticket.getScannedBy());

        return toResponse(ticket, ScanStatus.VALID);
    }

    private Optional<Ticket> resolve(ScanRequestDto request, boolean forUpdate) {

        String code = (request.getTicketCode() == null) ? null : request.getTicketCode().trim();
        Long userId = request.getUserId();

        boolean hasCode = code != null && !code.isEmpty();
        boolean hasUserId = userId != null;

        if (hasCode == hasUserId) {
            throw new IllegalArgumentException("Provide exactly one of ticketCode or userId");
        }

        if (hasCode) {
            return forUpdate ? ticketRepository.findByTicketCodeForUpdate(code)
                             : ticketRepository.findByTicketCode(code);
        }
        return forUpdate ? ticketRepository.findByUserIdForUpdate(userId)
                         : ticketRepository.findByUserId(userId);
    }

    private ScanResponseDto toResponse(Ticket ticket, ScanStatus status) {
        User user = ticket.getUser();
        return new ScanResponseDto(
                status,
                user.getId(),
                UserNameFormatter.fullName(user),
                ticket.getTicketTypeCode(),
                ticket.getScannedAt(),
                ticket.getScannedBy()
        );
    }
}
