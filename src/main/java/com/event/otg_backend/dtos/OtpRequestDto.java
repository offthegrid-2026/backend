package com.event.otg_backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpRequestDto {

    @NotBlank(message = "Email is required !!")
    @Email(message = "Invalid Email format !!")
    private String email;
}
