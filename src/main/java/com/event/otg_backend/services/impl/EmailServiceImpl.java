package com.event.otg_backend.services.impl;

import com.event.otg_backend.exceptions.EmailSendException;
import com.event.otg_backend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("OTG Verification Code");
            message.setText(
                    "Your OTP is:" + otp + "\n\n"+
                            "This code is valid for 5 minutes."+
                            "Please do not share it with anyone.\n\n"+
                            "If you didn't request this, you can safely ignore this email."
            );
            mailSender.send(message);
        }catch (MailException ex){
            throw new EmailSendException("Could not send the verification email. Please try again.");
        }
    }
}
