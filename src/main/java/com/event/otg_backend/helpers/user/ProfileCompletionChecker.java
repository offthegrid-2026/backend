package com.event.otg_backend.helpers.user;

import com.event.otg_backend.models.User;

public final class ProfileCompletionChecker {

    private ProfileCompletionChecker(){}

    public static boolean isComplete(User user){
        return hasText(user.getFirstName())
                && hasText(user.getLastName())
                && hasText(user.getPhoneNumber())
                && hasText(user.getCityState())
                && hasText(user.getCollegeOrOrg());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
