package com.event.otg_backend.helpers.security;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(){

        int otp = RANDOM.nextInt(1_000_000);
//        int otp = 123456; // should not be used in production, uncomment the above line for production
        return String.format("%06d", otp);
    }

}
