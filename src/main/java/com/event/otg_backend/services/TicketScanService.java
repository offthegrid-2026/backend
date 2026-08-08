package com.event.otg_backend.services;

import com.event.otg_backend.dtos.scan.ScanRequestDto;
import com.event.otg_backend.dtos.scan.ScanResponseDto;

public interface TicketScanService {

    /** Read-only: who is this, and have they already been admitted? */
    ScanResponseDto lookup(ScanRequestDto request);

    /** Marks the ticket as scanned. Refuses if it was already scanned. */
    ScanResponseDto checkIn(ScanRequestDto request);
}
