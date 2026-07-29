package com.event.otg_backend.services;

public interface OtpService {

    String generateAndStoreOtp(String email);

    void verifyOtp(String email, String submittedOtp);

    void invalidateOtp(String email);
}
