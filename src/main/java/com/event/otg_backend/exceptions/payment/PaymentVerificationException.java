package com.event.otg_backend.exceptions.payment;

public class PaymentVerificationException extends RuntimeException{
    public PaymentVerificationException(String message){
        super(message);
    }
}
