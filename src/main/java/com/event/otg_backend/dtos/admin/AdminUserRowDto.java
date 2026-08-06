package com.event.otg_backend.dtos.admin;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserRowDto {

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String cityState;
    private String collegeOrOrg;
    private Boolean paymentStatus;
    private Integer ticketId;
    private Boolean ticketEmailSent;
    private Instant createdAt;
}
