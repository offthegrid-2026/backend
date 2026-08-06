package com.event.otg_backend.exceptions.user;

public class ProfileLockedException extends RuntimeException{

    public ProfileLockedException(String message){
        super(message);
    }

    public ProfileLockedException(){
        super("Your profile is already completed and can no longer be edited.");
    }
}
