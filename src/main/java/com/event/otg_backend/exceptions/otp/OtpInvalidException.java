package com.event.otg_backend.exceptions.otp;

public class OtpInvalidException extends RuntimeException {

    public OtpInvalidException(String message) {
        super(message);
    }

    public OtpInvalidException(){
        super("Incorrect OTP. Please try again.");
    }
}
