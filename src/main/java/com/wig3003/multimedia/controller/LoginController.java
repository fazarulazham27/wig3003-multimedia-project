package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML private Label emailError;
    @FXML private Label passwordError;

    @FXML private HBox emailBox;
    @FXML private HBox passwordBox;

    // ---------------- LOGIN ----------------

    @FXML
    public void login() {

        clearErrors();

        String email = emailField.getText();
        String password = passwordField.getText();

        boolean valid = true;

        if (email == null || email.isBlank()) {
            showError(emailBox, emailError, "Email is required");
            valid = false;
        }

        if (password == null || password.isBlank()) {
            showError(passwordBox, passwordError, "Password is required");
            valid = false;
        }

        if (!valid) return;

        String response = AuthService.login(email, password);

        if (AuthService.isSuccess(response)) {
            goToDashboard();
        } else {
            showAlert("Login Failed", "Invalid email or password");
        }
    }

    // ---------------- NAVIGATION ----------------

    @FXML
    public void goToSignup() {
        loadScene("/fxml/signup-view.fxml", "Signup Page", 1000, 700);
    }

    public void goToDashboard() {
        loadScene("/fxml/repository-view.fxml", "All Photos", 1200, 800);
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
        emailError.setVisible(false);
        passwordError.setVisible(false);

        emailError.setManaged(false);
        passwordError.setManaged(false);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}