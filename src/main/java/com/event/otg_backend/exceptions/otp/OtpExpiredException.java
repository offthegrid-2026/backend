package com.event.otg_backend.exceptions.otp;

public class OtpExpiredException extends RuntimeException {

    public OtpExpiredException(String message) {
        super(message);
    }

    public OtpExpiredException(){
        super("OTP expired or not found. Please request a new one.");
    }
}
