package com.event.otg_backend.controllers;

import com.event.otg_backend.dtos.UserDto;
import com.event.otg_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }


    //just for checking
    @PatchMapping("/{userId}")
    public void markPaymentAsPaid(@PathVariable Long userId){
        userService.markPaymentAsPaid(userId);
    }
}
