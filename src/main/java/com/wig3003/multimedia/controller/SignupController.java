package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label nameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    @FXML private HBox nameBox;
    @FXML private HBox emailBox;
    @FXML private HBox passwordBox;
    @FXML private HBox confirmBox;

    // ---------------- SIGNUP ----------------

    @FXML
    public void signup() {

        clearErrors();

        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        boolean valid = true;

        if (email == null || email.isBlank()) {
            showError(emailBox, emailError, "Email required");
            valid = false;
        }

        if (password == null || password.isBlank()) {
            showError(passwordBox, passwordError, "Password required");
            valid = false;
        }

        if (!password.equals(confirm)) {
            showError(confirmBox, confirmPasswordError, "Passwords do not match");
            valid = false;
        }

        if (!valid) return;

        String response = AuthService.signup(email, password);

        // 🔥 HANDLE FIREBASE RESPONSE PROPERLY
        if (AuthService.isSuccess(response)) {

            showAlert("Success", "Account created successfully!");
            goToLogin();
            return;
        }

        // ❌ EXTRACT FIREBASE ERROR MESSAGE
        if (response.contains("WEAK_PASSWORD")) {
            showAlert("Signup Failed", "Password must be at least 6 characters");
            return;
        }

        if (response.contains("EMAIL_EXISTS")) {
            showAlert("Signup Failed", "Email already exists");
            return;
        }

        if (response.contains("INVALID_EMAIL")) {
            showAlert("Signup Failed", "Invalid email format");
            return;
        }

        // fallback
        showAlert("Signup Failed", "Something went wrong");
    }

    // ---------------- NAVIGATION ----------------

    @FXML
    public void goToLogin() {
        loadScene("/fxml/login-view.fxml", "Login", 1000, 700);
    }

    private void loadScene(String path, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene newScene = new Scene(loader.load());
            newScene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            
            stage.setScene(newScene);
            stage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- UI HELPERS ----------------

    private void showError(HBox box, Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {

        nameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);

        nameError.setManaged(false);
        emailError.setManaged(false);
        passwordError.setManaged(false);
        confirmPasswordError.setManaged(false);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}