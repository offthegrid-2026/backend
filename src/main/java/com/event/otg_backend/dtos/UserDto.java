package com.event.otg_backend.dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String cityState;
    private String collegeOrOrg;
    private Boolean paymentStatus = false;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}
