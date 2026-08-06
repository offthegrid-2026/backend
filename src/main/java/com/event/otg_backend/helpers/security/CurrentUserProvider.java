package com.event.otg_backend.helpers.security;

import com.event.otg_backend.exceptions.auth.InvalidTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserProvider {

    private CurrentUserProvider(){}

    public static Long getCurrentUserId(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long)){
            throw new InvalidTokenException("No authenticated user found");
        }

        return (Long) authentication.getPrincipal();
    }
}
