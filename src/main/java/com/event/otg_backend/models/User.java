package com.event.otg_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "user_sequence",
            initialValue = 20260,
            allocationSize = 1
    )
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String cityState;

    private String collegeOrOrg;

    @Column(columnDefinition = "boolean", nullable = false)
    private Boolean paymentStatus = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
