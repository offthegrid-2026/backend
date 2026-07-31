package com.event.otg_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String code;   // "EARLY_BIRD", "NORMAL"

    @Column(nullable = false)
    private String displayName;    // Early Bird, Normal

    @Column(nullable = false)
    private Long pricePaise;

    @Column(nullable = false)
    private Integer seatLimit;

    @Column(nullable = false)
    private Instant saleStartAt;

    @Column(nullable = false)
    private Instant saleEndAt;
}
