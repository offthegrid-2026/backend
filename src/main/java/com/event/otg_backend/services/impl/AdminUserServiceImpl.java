package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.admin.AdminCreateUserDto;
import com.event.otg_backend.dtos.admin.AdminUserPageDto;
import com.event.otg_backend.dtos.admin.AdminUserRowDto;
import com.event.otg_backend.dtos.user.ProfileUpdateDto;
import com.event.otg_backend.dtos.user.UserDto;
import com.event.otg_backend.exceptions.ResourceNotFoundException;
import com.event.otg_backend.models.Ticket;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.TicketRepository;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.AdminUserService;
import com.event.otg_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketDeliveryService ticketDeliveryService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminUserPageDto listUsers(String q, int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        String search = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";

        Page<AdminUserRowDto> result =
                userRepository.findAdminRows(search, PageRequest.of(safePage, safeSize));

        return AdminUserPageDto.builder()
                .users(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }


    @Override
    @Transactional
    public UserDto createUser(AdminCreateUserDto dto) {

        // Same normalization as the OTP login path - otherwise an admin-created
        // "Foo@X.com" would never match the "foo@x.com" that login looks up.
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCityState(dto.getCityState());
        user.setCollegeOrOrg(dto.getCollegeOrOrg());
        user.setPaymentStatus(false);

        User saved = userRepository.save(user);
        log.info("Admin created user id={}, email={}", saved.getId(), saved.getEmail());

        return modelMapper.map(saved, UserDto.class);
    }


    @Override
    @Transactional
    public UserDto updateUser(Long userId, ProfileUpdateDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        // Deliberately no ProfileCompletionChecker/ProfileLockedException check here:
        // that lock stops users editing themselves after payment, and overriding it
        // is exactly what the admin edit button is for.
        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getCityState() != null)
            user.setCityState(dto.getCityState());
        if (dto.getCollegeOrOrg() != null)
            user.setCollegeOrOrg(dto.getCollegeOrOrg());

        User saved = userRepository.save(user);
        log.info("Admin updated profile for user id={}", userId);

        return modelMapper.map(saved, UserDto.class);
    }


    // No @Transactional here on purpose - resend() opens its own write transaction.
    @Override
    public void resendTicket(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Ticket ticket = ticketRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No ticket has been issued for this user yet."));

        ticketDeliveryService.resend(ticket.getId());
    }
}
