package com.event.otg_backend.services.impl;

import com.event.otg_backend.exceptions.EmailSendException;
import com.event.otg_backend.exceptions.ResourceNotFoundException;
import com.event.otg_backend.helpers.ticket.QrCodeGenerator;
import com.event.otg_backend.helpers.ticket.TicketPdfGenerator;
import com.event.otg_backend.helpers.user.UserNameFormatter;
import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.services.EmailService;
import com.event.otg_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class TicketDeliveryServiceImpl implements TicketDeliveryService {

    private final TicketRepository ticketRepository;
    private final EmailService emailService;


    @Override
    @Transactional
    public void deliver(int ticketId) {

        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElse(null);
        if (ticket == null){
            log.warn("Ticket {} not found for email delivery", ticketId);
            return;
        }
        if (ticket.isEmailSent()){
            log.debug("Ticket {} email already sent - skipping", ticketId);
            return;
        }

        send(ticket);


    }


    @Override
    @Transactional
    public void resend(int ticketId) {

        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(()-> new ResourceNotFoundException("Ticket not found"));
        send(ticket);
        log.info("Ticket {} re-sent by admin", ticketId);
    }

    private void send(Ticket ticket) {

        User user = ticket.getUser();
        String fullName = UserNameFormatter.fullName(user);

        try {
            byte[] qr = QrCodeGenerator.generateQrImage(ticket.getTicketCode(), 300, 300);
            byte[] pdf = TicketPdfGenerator.generate(user.getId(), fullName, qr);
            emailService.sendTicketEmail(user.getEmail(), fullName, pdf);
        }catch (EmailSendException e){
            log.error("Ticket delivery failed for ticketId={}: {}", ticket.getId(), e.getMessage(), e);
            throw e;
        }catch (Exception e){
            log.error("Ticket delivery failed for ticketId={}: {}", ticket.getId(), e.getMessage(), e);
            throw new RuntimeException("Ticket delivery failed", e);
        }

        ticket.setEmailSent(true);
        ticketRepository.save(ticket);
        log.info("Ticket email delivered: ticketId={}, to={}", ticket.getId(), user.getEmail());
    }
}
