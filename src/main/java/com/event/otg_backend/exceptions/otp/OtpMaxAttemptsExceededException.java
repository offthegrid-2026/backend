package com.event.otg_backend.exceptions.otp;

public class OtpMaxAttemptsExceededException extends RuntimeException {

    public OtpMaxAttemptsExceededException(String message) {
        super(message);
    }

    public OtpMaxAttemptsExceededException(){
        super("Too many incorrect attempts. Please request a new OTP.");
    }
}
