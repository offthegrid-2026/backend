package com.event.otg_backend.dtos.scan;

import java.time.Instant;

public record ScanResponseDto(
        ScanStatus status,
        Long userId,
        String name,
        String ticketTypeCode,
        Instant scannedAt,
        String scannedBy
) {
    public static ScanResponseDto notFound() {
        return new ScanResponseDto(ScanStatus.NOT_FOUND, null, null, null, null, null);
    }
}
