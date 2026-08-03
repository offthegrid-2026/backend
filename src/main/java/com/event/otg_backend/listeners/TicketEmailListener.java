package com.event.otg_backend.listeners;

import com.event.otg_backend.events.TicketConfirmedEvent;
import com.event.otg_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEmailListener {

    private final TicketDeliveryService ticketDeliveryService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketConfirmed(TicketConfirmedEvent event) {
        try {
            ticketDeliveryService.deliver(event.ticketId());
        }catch (Exception e){
            log.error("Async ticket delivery threw for ticketId={}: {}", event.ticketId(), e.getMessage(), e);
        }
    }
}
