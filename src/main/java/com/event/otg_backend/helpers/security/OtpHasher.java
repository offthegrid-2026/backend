package com.event.otg_backend.helpers.security;

//hashing and verifying OTPs

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OtpHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String raw)   {
        return encoder.encode(raw);
    }

    public boolean matches(String raw, String storedHash){
        return encoder.matches(raw, storedHash);
    }

}