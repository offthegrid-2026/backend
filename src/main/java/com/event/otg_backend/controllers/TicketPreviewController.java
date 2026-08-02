package com.event.otg_backend.controllers;

import com.event.otg_backend.helpers.QrCodeGenerator;
import com.event.otg_backend.helpers.TicketPdfGenerator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketPreviewController {

    // TEMP dev preview — remove before production
    @GetMapping("/ticket-preview")
    public ResponseEntity<byte[]> preview() throws Exception {
        byte[] qr = QrCodeGenerator.generateQrImage("SAMPLE1234567890SAMPLE1234567890", 300, 300);
        byte[] pdf = TicketPdfGenerator.generate(20260L, "John Doe", qr);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
