package com.event.otg_backend.services;

import com.event.otg_backend.dtos.ticket.TicketTypePublicDto;

import java.util.List;

public interface TicketTypeService {

    /** Public, read-only projection for attendees browsing the store. */
    List<TicketTypePublicDto> listPurchasableTicketTypes();
}
