package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.AuthService;
import com.wig3003.multimedia.service.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
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
            SessionManager.setEmail(email);
            goToDashboard(email);
        } else {
            showAlert("Login Failed", "Invalid email or password");
        }
    }

    // ---------------- NAVIGATION ----------------

    @FXML
    public void goToSignup() {
        loadScene("/fxml/signup-view.fxml", "Signup Page");
    }

    public void goToDashboard(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/repository-view.fxml")
            );

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene newScene = new Scene(loader.load(),
                screen.getWidth(), screen.getHeight());
            newScene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm()
            );

            // Pass email to SocialSharingController if it is the loaded controller
            // If repository loads first, store email in a shared session instead
            Object controller = loader.getController();
            if (controller instanceof SocialSharingController socialCtrl) {
                socialCtrl.setLoggedInEmail(email);
            }

            stage.setScene(newScene);
            stage.setTitle("PhotoManager");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene newScene = new Scene(loader.load(),
                screen.getWidth(), screen.getHeight());
            newScene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm()
            );

            stage.setScene(newScene);
            stage.setTitle(title);
            stage.setMaximized(true);

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