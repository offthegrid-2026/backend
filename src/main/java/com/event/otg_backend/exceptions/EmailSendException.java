package com.event.otg_backend.exceptions;

public class EmailSendException extends RuntimeException {

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(){
        super("Failed to send email. Please try again.");
    }

}
