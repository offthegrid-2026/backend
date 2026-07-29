package com.event.otg_backend.services;


import com.event.otg_backend.dtos.AuthResponseDto;

public interface AuthService {

    void requestOtp(String email);

    AuthResponseDto verifyOtpAndAuthenticate(String email, String otp);
}
