package com.event.otg_backend.services;

import com.event.otg_backend.dtos.admin.AdminCreateUserDto;
import com.event.otg_backend.dtos.admin.AdminUserPageDto;
import com.event.otg_backend.dtos.user.ProfileUpdateDto;
import com.event.otg_backend.dtos.user.UserDto;

public interface AdminUserService {

    AdminUserPageDto listUsers(String q, int page, int size);

    UserDto createUser(AdminCreateUserDto dto);

    UserDto updateUser(Long userId, ProfileUpdateDto dto);

    void resendTicket(Long userId);
}
