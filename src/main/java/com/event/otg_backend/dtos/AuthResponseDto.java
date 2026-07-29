package com.event.otg_backend.dtos;

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
}
