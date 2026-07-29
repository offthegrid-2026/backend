package com.event.otg_backend.services;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}
