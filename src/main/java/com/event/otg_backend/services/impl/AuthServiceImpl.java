package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.auth.AuthResponseDto;
import com.event.otg_backend.exceptions.EmailSendException;
import com.event.otg_backend.helpers.security.JwtService;
import com.event.otg_backend.helpers.user.ProfileCompletionChecker;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.AuthService;
import com.event.otg_backend.services.EmailService;
import com.event.otg_backend.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${spring.app.scanner.emails:}")
    private String[] scannerEmails;

    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void requestOtp(String email) {

        String normalizedEmail = normalize(email);

        String otp = otpService.generateAndStoreOtp(normalizedEmail);

        try {
            emailService.sendOtpEmail(normalizedEmail, otp);
        }catch (EmailSendException ex){
            otpService.invalidateOtp(normalizedEmail);
            throw ex; //-> Frontend can prompt "Try again"
        }
    }

    @Override
    @Transactional
    public AuthResponseDto verifyOtpAndAuthenticate(String email, String otp) {

        String normalizedEmail = normalize(email);

        otpService.verifyOtp(normalizedEmail, otp);

        if (isScannerEmail(normalizedEmail)){
            String token = jwtService.generateScannerToken(normalizedEmail);
            return AuthResponseDto.builder()
                    .token(token)
                    .email(normalizedEmail)
                    .scanner(true)
                    .build();
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> createBareUser(normalizedEmail));

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .profileCompleted(ProfileCompletionChecker.isComplete(user))
                .build();
    }

    private boolean isScannerEmail(String email) {
        return Arrays.stream(scannerEmails)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.equalsIgnoreCase(email));
    }

    @Override
    public void logout(String token) {
        jwtService.revokeToken(token);
    }

    private User createBareUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPaymentStatus(false);
        return userRepository.save(user);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
