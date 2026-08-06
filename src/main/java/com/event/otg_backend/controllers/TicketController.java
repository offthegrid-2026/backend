package com.event.otg_backend.controllers;

import com.event.otg_backend.helpers.security.CurrentUserProvider;
import com.event.otg_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/me/pdf")
    public ResponseEntity<byte[]> downloadMyTicket(){
        Long userId = CurrentUserProvider.getCurrentUserId();
        byte[] pdf = ticketService.getTicketPdfForUser(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"otg-ticket.pdf\"")
                .body(pdf);
    }

}
