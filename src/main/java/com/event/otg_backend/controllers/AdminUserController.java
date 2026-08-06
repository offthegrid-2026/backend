package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.admin.AdminUserPageDto;
import com.event.otg_backend.dtos.user.ProfileUpdateDto;
import com.event.otg_backend.dtos.user.UserDto;
import com.event.otg_backend.services.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<AdminUserPageDto> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ){
        return ResponseEntity.ok(adminUserService.listUsers(q, page, size));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @Valid @RequestBody ProfileUpdateDto request){
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    @PostMapping("/{userId}/resend-ticket")
    public ResponseEntity<Void> resendTicket(@PathVariable Long userId) {
        adminUserService.resendTicket(userId);
        return ResponseEntity.noContent().build();
    }
}
