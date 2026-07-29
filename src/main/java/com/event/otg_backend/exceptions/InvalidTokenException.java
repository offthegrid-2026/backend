package com.event.otg_backend.exceptions;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(){
        super("Invalid or expired token");
    }
}
