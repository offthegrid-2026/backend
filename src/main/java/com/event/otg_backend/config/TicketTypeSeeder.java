package com.event.otg_backend.config;

import com.event.otg_backend.models.TicketType;
import com.event.otg_backend.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// First-deploy defaults only. Inserts a ticket type if its code is missing and never
// touches an existing row, so a price or date changed later in the database survives
// every restart. Editing the values here does NOT update a database that already has them.
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketTypeSeeder implements ApplicationRunner {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final LocalTime LAST_MICROSECOND = LocalTime.MAX.truncatedTo(ChronoUnit.MICROS);

    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("EARLY_BIRD", "Early Bird", 49_900L, 300,
                LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 31));

        seedIfMissing("NORMAL", "Normal", 79_900L, 700,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    private void seedIfMissing(String code, String displayName, long pricePaise, int seatLimit,
                               LocalDate saleStartDate, LocalDate saleEndDate) {

        if (ticketTypeRepository.findByCode(code).isPresent()) {
            return;
        }

        TicketType type = new TicketType();
        type.setCode(code);
        type.setDisplayName(displayName);
        type.setPricePaise(pricePaise);
        type.setSeatLimit(seatLimit);
        // Sales open at the start of the first day and close at the end of the last day, IST.
        // End time is truncated to microseconds: Postgres stores micros and would round
        // LocalTime.MAX's .999999999 up to midnight of the NEXT day.
        type.setSaleStartAt(saleStartDate.atStartOfDay(IST).toInstant());
        type.setSaleEndAt(saleEndDate.atTime(LAST_MICROSECOND).atZone(IST).toInstant());

        ticketTypeRepository.save(type);
        log.info("Seeded ticket type {}: {} paise, {} seats, {} to {} IST",
                code, pricePaise, seatLimit, saleStartDate, saleEndDate);
    }
}
