package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.auth.AuthResponseDto;
import com.event.otg_backend.dtos.auth.OtpRequestDto;
import com.event.otg_backend.dtos.auth.OtpVerifyDto;
import com.event.otg_backend.exceptions.auth.InvalidTokenException;
import com.event.otg_backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String , String>> requestOtp(@Valid @RequestBody OtpRequestDto request){

        authService.requestOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "An OTP has been sent to your email."));

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyDto request){
        AuthResponseDto response = authService.verifyOtpAndAuthenticate(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader (value = "Authorization", required = false) String authHeader){

        if( authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new InvalidTokenException("No authentication token provided.");
        }

        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

}
