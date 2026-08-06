package com.event.otg_backend.controllers;

import com.event.otg_backend.helpers.ticket.QrCodeGenerator;
import com.event.otg_backend.helpers.ticket.TicketPdfGenerator;
import com.event.otg_backend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TicketPreviewController {

    private final EmailService emailService;

    // TEMP dev preview — remove before production
    @GetMapping("/ticket-preview")
    public ResponseEntity<byte[]> preview() throws Exception {
        byte[] qr = QrCodeGenerator.generateQrImage("SAMPLE1234567890SAMPLE1234567890", 300, 300);
        byte[] pdf = TicketPdfGenerator.generate(20260L, "John Doe", qr);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/ticket-email-test")
    public ResponseEntity<String> emailTest(@RequestParam String to) throws Exception {
        byte[] qr = QrCodeGenerator.generateQrImage("SAMPLE1234567890SAMPLE1234567890", 300, 300);
        byte[] pdf = TicketPdfGenerator.generate(20260L, "John Doe", qr);
        emailService.sendTicketEmail(to, "John Doe", pdf);
        return ResponseEntity.ok("Ticket email sent to " + to);
    }
}
