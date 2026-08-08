package com.event.otg_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Double price;

    @Column(unique = true, nullable = false, length = 40)
    private String ticketCode;

    @Column(length = 30)
    private String ticketTypeCode;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean emailSent = false;

    // Gate check-in. null scannedAt = not yet scanned; one field so the two can never disagree.
    private Instant scannedAt;

    @Column(length = 120)
    private String scannedBy;          // staff email that admitted them

    @CreationTimestamp
    @Column(updatable = false)
    private Instant timestamp;
}
