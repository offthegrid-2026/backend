package com.event.otg_backend.services;

import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;

public interface TicketService {

    Ticket generateTicketForUser(User user, String ticketTypeCode, Long amountPaise);
}
