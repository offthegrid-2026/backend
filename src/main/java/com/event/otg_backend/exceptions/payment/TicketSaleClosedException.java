package com.event.otg_backend.exceptions.payment;

public class TicketSaleClosedException extends RuntimeException {
    public TicketSaleClosedException(String message) {
        super(message);
    }
}
