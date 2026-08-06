package com.event.otg_backend.services;

import com.event.otg_backend.dtos.user.ProfileUpdateDto;
import com.event.otg_backend.dtos.user.UserDto;
import com.event.otg_backend.models.User;

import java.util.List;

public interface UserService {

    User findOrCreateByEmail(String email);

    UserDto getUserById(Long userId);

    UserDto updateProfile(Long userId, ProfileUpdateDto profileUpdateDto);

    void markPaymentAsPaid(Long userId);

}
