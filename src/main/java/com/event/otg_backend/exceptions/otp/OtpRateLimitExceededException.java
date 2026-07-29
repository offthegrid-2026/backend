package com.event.otg_backend.exceptions.otp;

public class OtpRateLimitExceededException extends RuntimeException {

    public OtpRateLimitExceededException(String message) {
        super(message);
    }

    public OtpRateLimitExceededException(){
        super("Too many OTP requests. Please try again later.");
    }
}
