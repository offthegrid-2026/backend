package com.event.otg_backend.helpers;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(){

        int otp = RANDOM.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

}
