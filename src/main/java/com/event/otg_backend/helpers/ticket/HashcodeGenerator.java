package com.event.otg_backend.helpers.ticket;

import java.security.SecureRandom;

//GENERATED HASHCODE FOR THE TICKET WHICH WILL BE CONVERTED INTO QR

public class HashcodeGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length){
        StringBuilder sb = new StringBuilder(length);
        for( int i=0; i<length; i++)
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));

        return sb.toString();
    }
}
