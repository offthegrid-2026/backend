package com.event.otg_backend.dtos.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDto {

    private String token;
    private Long userId;
    private String email;
    private boolean profileCompleted;
    private boolean admin;
    private boolean scanner;
}
