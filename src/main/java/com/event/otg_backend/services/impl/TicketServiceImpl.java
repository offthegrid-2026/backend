package com.event.otg_backend.services.impl;

import com.event.otg_backend.helpers.HashcodeGenerator;
import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final int TICKET_CODE_LENGTH = 12;

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Ticket generateTicketForUser(User user, String ticketTypeCode, Long amountPaise) {
        return ticketRepository.findByUser(user).orElseGet(()->{
            Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setTicketTypeCode(ticketTypeCode);
            ticket.setPrice(amountPaise/100.0);
            ticket.setTicketCode(generateUniqueTicketCode());

            user.setPaymentStatus(true);
            userRepository.save(user);

            return ticketRepository.save(ticket);
        });
    }

    private String generateUniqueTicketCode() {
        String code;

        do{
            code = HashcodeGenerator.generate(TICKET_CODE_LENGTH);
        }while(ticketRepository.existsByTicketCode(code));
        return code;
    }
}
