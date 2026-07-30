package com.event.otg_backend.services.impl;

import com.event.otg_backend.dtos.ProfileUpdateDto;
import com.event.otg_backend.dtos.UserDto;
import com.event.otg_backend.exceptions.ProfileLockedException;
import com.event.otg_backend.exceptions.ResourceNotFoundException;
import com.event.otg_backend.helpers.ProfileCompletionChecker;
import com.event.otg_backend.models.User;
import com.event.otg_backend.repository.UserRepository;
import com.event.otg_backend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public User findOrCreateByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(()-> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPaymentStatus(false);
                    return userRepository.save(newUser);
                });

    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long userId, ProfileUpdateDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));


        if(ProfileCompletionChecker.isComplete(user)){
            throw new ProfileLockedException();
        }

        if(dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if(dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if(dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());
        if(dto.getCity() != null)
            user.setCity(dto.getCity());
        if(dto.getCollegeOrOrg() != null)
            user.setCollegeOrOrg(dto.getCollegeOrOrg());

        User saved = userRepository.save(user);
        return modelMapper.map(saved, UserDto.class);
    }

    @Override
    @Transactional
    public void markPaymentAsPaid(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        user.setPaymentStatus(true);
        userRepository.save(user);
    }
}
