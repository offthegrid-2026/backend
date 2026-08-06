package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.user.ProfileUpdateDto;
import com.event.otg_backend.dtos.user.UserDto;
import com.event.otg_backend.helpers.security.CurrentUserProvider;
import com.event.otg_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    //fetch user details
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(){
        Long userId = CurrentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(userService.getUserById(userId));
    }


    //update profile
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody ProfileUpdateDto request){
        Long userId = CurrentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }
}

