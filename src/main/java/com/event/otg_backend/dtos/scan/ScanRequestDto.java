package com.event.otg_backend.dtos.scan;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanRequestDto {

    // Exactly one of these must be supplied - enforced in TicketScanServiceImpl.resolve().
    @Size(max = 64, message = "Invalid ticket code")
    private String ticketCode;

    private Long userId;
}
