package com.event.otg_backend.services.impl;

import com.event.otg_backend.exceptions.EmailSendException;
import com.event.otg_backend.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
                    "Your OTP is:  " + otp + "\n\n"+
                            "This code is valid for 5 minutes. "+ "Please do not share it with anyone.\n\n"+
                            "If you didn't request this, you can safely ignore this email."
            );
            mailSender.send(message);
        }catch (MailException ex){
            throw new EmailSendException("Could not send the verification email. Please try again.");
        }
    }

    @Override
    public void sendTicketEmail(String toEmail, String fullName, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your OTG Ticket");
            helper.setText(
                    "<p>Hi " + escapeHtml(fullName) + ",</p>" +
                            "<p>Your payment was successful and your OTG ticket is attached below.</p>" +
                            "<p><b>Please show the QR code on your ticket at the entry gate.</b></p>" +
                            "<p>See you at the event!</p>",
                    true
            );
            helper.addAttachment("OTG-Ticket.pdf", new ByteArrayResource(pdfBytes), "application/pdf");
            mailSender.send(message);
            log.info("Ticket email sent to {}", toEmail);
        }catch (MessagingException | MailException ex){
            log.error("Failed to send ticket email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new EmailSendException("Could not send the ticket email. Please try again.");
        }
    }
    private String escapeHtml(String s){
        if(s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
