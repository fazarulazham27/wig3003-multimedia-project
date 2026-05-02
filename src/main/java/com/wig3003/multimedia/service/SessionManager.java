package com.wig3003.multimedia.service;

public class SessionManager {

    private static String loggedInEmail = "";

    public static void setEmail(String email) {
        loggedInEmail = email;
    }

    public static String getEmail() {
        return loggedInEmail;
    }

    //  ADDED LOGOUT
    public static void clear() {
        loggedInEmail = "";
    }

    // Optional helper
    public static boolean isLoggedIn() {
        return loggedInEmail != null && !loggedInEmail.isBlank();
    }
}