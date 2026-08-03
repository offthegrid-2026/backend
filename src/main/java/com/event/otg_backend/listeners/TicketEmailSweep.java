package com.event.otg_backend.listeners;

import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEmailSweep {

    private final TicketRepository ticketRepository;
    private final TicketDeliveryService ticketDeliveryService;

    @Value("${spring.app.ticket.resend-after-minutes}")
    private long resendAfterMinutes;

    // Backstop for the async listener: retries any confirmed ticket whose email never went out
    // (app crash between save & send, or a transient SMTP failure). deliver() is idempotent + row-locked,
    // so this can never double-send even if it races the async listener.
    @Scheduled(fixedDelayString = "${spring.app.ticket.resend-sweep-ms}")
    public void resendPendingTickets() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(resendAfterMinutes));
        List<Ticket> pending = ticketRepository.findByEmailSentFalseAndTimestampBefore(threshold);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Ticket resend sweep: found {} unsent ticket(s)", pending.size());
        for (Ticket ticket : pending) {
            try {
                ticketDeliveryService.deliver(ticket.getId());
            } catch (Exception e) {
                log.error("Sweep resend failed for ticketId={}: {}", ticket.getId(), e.getMessage(), e);
            }
        }
    }
}
