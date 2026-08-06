package com.event.otg_backend.dtos.admin;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserPageDto {

    private List<AdminUserRowDto> users;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
