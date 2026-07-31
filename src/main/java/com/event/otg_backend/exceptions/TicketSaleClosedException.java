package com.event.otg_backend.exceptions;

public class TicketSaleClosedException extends RuntimeException {
    public TicketSaleClosedException(String message) {
        super(message);
    }
}
