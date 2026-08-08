package com.event.otg_backend.dtos.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketTypeUpdateDto {

    @Size(max = 100, message = "Display name too long")
    private String displayName;

    // Entered in rupees; converted to paise before it touches the database.
    // BigDecimal, never double - binary floating point cannot represent money exactly.
    @DecimalMin(value = "1.00", message = "Price must be at least ₹1")
    @Digits(integer = 7, fraction = 2, message = "Price can have at most 2 decimal places")
    private BigDecimal priceRupees;

    @Min(value = 0, message = "Seat limit cannot be negative")
    private Integer seatLimit;

    // Plain dates ("2026-09-01"), interpreted as Indian time (Asia/Kolkata).
    private LocalDate saleStartDate;
    private LocalDate saleEndDate;
}
