package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.AuthResponseDto;
import com.event.otg_backend.exceptions.EmailSendException;
import com.event.otg_backend.helpers.JwtService;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.AuthService;
import com.event.otg_backend.services.EmailService;
import com.event.otg_backend.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

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

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> createBareUser(normalizedEmail));

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .profileCompleted(isProfileCompleted(user))
                .build();
    }

    private boolean isProfileCompleted(User user) {
        return hasText(user.getFirstName())
                && hasText(user.getLastName())
                && hasText(user.getPhoneNumber())
                && hasText(user.getCity())
                && hasText((user.getCollegeOrOrg()));
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
