package com.event.otg_backend.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateDto {

    @Size(max = 50, message = "First name too long")
    private String firstName;

    @Size(max = 50, message = "Last name too long")
    private String lastName;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Size(max = 60)
    private String cityState;

    @Size(max = 100)
    private String collegeOrOrg;
}
