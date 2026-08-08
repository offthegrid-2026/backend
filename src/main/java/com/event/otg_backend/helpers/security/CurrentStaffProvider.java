package com.event.otg_backend.helpers.security;

import com.event.otg_backend.exceptions.auth.InvalidTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentStaffProvider {

    private CurrentStaffProvider(){}

    public static String getCurrentStaffEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !(authentication.getPrincipal() instanceof String)){
            throw new InvalidTokenException("No authenticated staff member found");
        }
        return (String) authentication.getPrincipal();
    }
}
