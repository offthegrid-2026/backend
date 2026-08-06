package com.event.otg_backend.services;

public interface TicketDeliveryService {

    void deliver(int ticketId);

    void resend(int ticketId);
}
