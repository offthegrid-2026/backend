package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.admin.TicketTypeConfigDto;
import com.event.otg_backend.dtos.admin.TicketTypeUpdateDto;
import com.event.otg_backend.services.AdminTicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ticket-types")
@RequiredArgsConstructor
public class AdminTicketTypeController {

    private final AdminTicketTypeService adminTicketTypeService;

    @GetMapping
    public ResponseEntity<List<TicketTypeConfigDto>> listTicketTypes() {
        return ResponseEntity.ok(adminTicketTypeService.listTicketTypes());
    }

    @PatchMapping("/{code}")
    public ResponseEntity<TicketTypeConfigDto> updateTicketType(
            @PathVariable String code,
            @Valid @RequestBody TicketTypeUpdateDto request) {

        return ResponseEntity.ok(adminTicketTypeService.updateTicketType(code, request));
    }
}
