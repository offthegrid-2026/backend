package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.ticket.TicketTypePublicDto;
import com.event.otg_backend.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Authenticated-user listing for the store. Falls under the SecurityConfig
// default anyRequest().authenticated(), same as /users/me and /payments/orders.
@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping
    public ResponseEntity<List<TicketTypePublicDto>> listTicketTypes() {
        return ResponseEntity.ok(ticketTypeService.listPurchasableTicketTypes());
    }
}
