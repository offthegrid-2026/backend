package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.scan.ScanRequestDto;
import com.event.otg_backend.dtos.scan.ScanResponseDto;
import com.event.otg_backend.services.TicketScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
public class ScanController {

    private final TicketScanService ticketScanService;

    @PostMapping("/lookup")
    public ResponseEntity<ScanResponseDto> lookup(@Valid @RequestBody ScanRequestDto request) {
        return ResponseEntity.ok(ticketScanService.lookup(request));
    }

    @PostMapping("/check-in")
    public ResponseEntity<ScanResponseDto> checkIn(@Valid @RequestBody ScanRequestDto request) {
        return ResponseEntity.ok(ticketScanService.checkIn(request));
    }
}
