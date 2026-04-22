package com.wig3003.multimedia.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AuthService {
    private static final String API_KEY =
            "AIzaSyBNRuZPedhMDWSez6LBOVXBGETZ7YoVGik";

    public static String login(String email, String password) {
        return sendRequest(email, password,
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=");
    }

    public static String signup(String email, String password) {
        return sendRequest(email, password,
                "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=");
    }

    private static String sendRequest(String email, String password, String urlBase) {
        try {
            URL url = new URL(urlBase + API_KEY);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = """
                {
                  "email": "%s",
                  "password": "%s",
                  "returnSecureToken": true
                }
            """.formatted(email, password);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            Scanner sc = new Scanner(
                    conn.getResponseCode() < 400
                            ? conn.getInputStream()
                            : conn.getErrorStream()
            );

            String response = sc.useDelimiter("\\A").next();
            sc.close();

            return response;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    public static boolean isSuccess(String response) {
        return response != null && response.contains("idToken");
    }
}
