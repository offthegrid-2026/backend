package com.event.otg_backend.helpers.user;

import com.event.otg_backend.models.User;

public final class UserNameFormatter {

    private UserNameFormatter() {}

    public static String fullName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        return (first + " " + last).trim();
    }
}
