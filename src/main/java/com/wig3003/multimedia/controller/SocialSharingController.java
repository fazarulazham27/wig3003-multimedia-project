package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.SessionManager;
import com.wig3003.multimedia.service.PhotoLibraryService;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SocialSharingController {

    // ── STATIC PENDING FILE HOLDER ────────────────────
    private static java.io.File pendingShareFile = null;

    public static void setPendingShareFile(java.io.File file) {
        pendingShareFile = file;
    }

    private static java.io.File getPendingShareFile() {
        return pendingShareFile;
    }

    private static void clearPendingShareFile() {
        pendingShareFile = null;
    }

    // ── STATIC PENDING TAB HOLDER ─────────────────────
    private static String pendingTab = null;

    public static void setPendingTab(String tab) {
        pendingTab = tab; // "email" or "whatsapp"
    }

    private static String getPendingTab() {
        return pendingTab;
    }

    private static void clearPendingTab() {
        pendingTab = null;
    }

    // ── FXML FIELDS ────────────────────────────────────

    // Email
    @FXML private TextField     senderEmailField;
    @FXML private TextField     recipientEmailField;
    @FXML private TextField     emailSubjectField;
    @FXML private TextArea      emailMessageArea;
    @FXML private VBox          emailAttachmentList;
    @FXML private Label         emailAttachHint;
    @FXML private Label         emailStatusLabel;

    // WhatsApp
    @FXML private TextArea      waMessageArea;
    @FXML private VBox          waPhotoList;
    @FXML private Label         waPhotoHint;
    @FXML private Label         waStatusLabel;

    // Tab controls
    @FXML private Button        emailTabBtn;
    @FXML private Button        waTabBtn;
    @FXML private ScrollPane    emailPanel;
    @FXML private ScrollPane    waPanel;

    // Side nav
    @FXML private SideNavBarController sideNavBarController;

    // ── STATE ──────────────────────────────────────────

    private final List<File> emailAttachments = new ArrayList<>();
    private final List<File> waPhotos         = new ArrayList<>();

    private String configEmail    = "";
    private String configPassword = "";

    private static final Path CONFIG_PATH = Paths.get(
        System.getProperty("user.home"), ".photomanager", "config.properties"
    );

    // ── TAB STYLES ─────────────────────────────────────

    private static final String TAB_ACTIVE =
        "-fx-background-color: white; -fx-text-fill: #5A3C2E; -fx-font-weight: bold;" +
        "-fx-font-size: 13px; -fx-background-radius: 10 10 0 0; -fx-border-radius: 10 10 0 0;" +
        "-fx-border-color: #E8DDD5 #E8DDD5 transparent #E8DDD5; -fx-border-width: 1;" +
        "-fx-padding: 10 24; -fx-cursor: hand;";

    private static final String TAB_INACTIVE =
        "-fx-background-color: transparent; -fx-text-fill: #8B6E5C;" +
        "-fx-font-size: 13px; -fx-background-radius: 10 10 0 0;" +
        "-fx-border-color: transparent; -fx-border-width: 1;" +
        "-fx-padding: 10 24; -fx-cursor: hand;";

    // ── INIT ───────────────────────────────────────────

    @FXML
    public void initialize() {
        // Load saved credentials first
        loadConfig();

        // Auto-fill sender — config first, SessionManager as fallback
        String email = !configEmail.isBlank() ? configEmail : SessionManager.getEmail();
        if (senderEmailField != null) {
            senderEmailField.setText(email);
        }

        // Pre-fill defaults
        emailSubjectField.setText("Check out my photo!");
        emailMessageArea.setText("Hey! I wanted to share this photo with you 😊");
        waMessageArea.setText("Check out this photo from PhotoManager! 📸");

        if (sideNavBarController != null) {
            sideNavBarController.setActiveModuleButton("social");
        }
        java.io.File pendingFile = getPendingShareFile();
        String tab = getPendingTab();

        if (pendingFile != null && pendingFile.exists()) {
            if ("email".equals(tab)) {
                addEmailAttachment(pendingFile);
            } else if ("whatsapp".equals(tab)) {
                addWhatsAppPhoto(pendingFile);
            }
            clearPendingShareFile();
        }

        if (tab != null) {
            if ("email".equals(tab)) {
                switchToEmail();
            } else if ("whatsapp".equals(tab)) {
                switchToWhatsApp();
            }
            clearPendingTab();
        }
    }

    // ── CONFIG ─────────────────────────────────────────

    private void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (var is = Files.newInputStream(CONFIG_PATH)) {
                Properties props = new Properties();
                props.load(is);
                configEmail    = props.getProperty("sender.email", "");
                configPassword = props.getProperty("sender.app.password", "");
                System.out.println("Config loaded for: " + configEmail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // First time — no saved credentials, show setup
        if (configEmail.isBlank() || configPassword.isBlank()) {
            Platform.runLater(this::showFirstTimeSetup);
        }
    }

    private void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Properties props = new Properties();
            props.setProperty("sender.email", configEmail);
            props.setProperty("sender.app.password", configPassword);
            try (var out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "PhotoManager Email Config");
            }
            System.out.println("Config saved to: " + CONFIG_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFirstTimeSetup() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Email Setup");
        dialog.setHeaderText(
            "One-time email setup\n" +
            "Your credentials are saved locally — you won't be asked again."
        );

        ButtonType saveBtn = new ButtonType("Save & Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField emailInput = new TextField();
        emailInput.setPromptText("yourgmail@gmail.com");
        emailInput.setStyle("-fx-padding: 10; -fx-font-size: 13px;");

        // Pre-fill from SessionManager if available
        String sessionEmail = SessionManager.getEmail();
        if (!sessionEmail.isBlank()) {
            emailInput.setText(sessionEmail);
        }

        PasswordField pwInput = new PasswordField();
        pwInput.setPromptText("xxxx xxxx xxxx xxxx");
        pwInput.setStyle("-fx-padding: 10; -fx-font-size: 13px;");

        Label hint = new Label(
            "💡 To generate an App Password:\n" +
            "Google Account → Security → 2-Step Verification → App Passwords\n" +
            "Select app: Mail, device: Windows Computer"
        );
        hint.setStyle("-fx-text-fill: #8B6E5C; -fx-font-size: 12px;");
        hint.setWrapText(true);

        VBox content = new VBox(10,
            new Label("Gmail Address"),
            emailInput,
            new Label("Gmail App Password"),
            pwInput,
            hint
        );
        content.setPadding(new Insets(16));
        content.setPrefWidth(400);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(440);

        dialog.setResultConverter(btn -> btn == saveBtn);
        dialog.showAndWait().ifPresent(confirmed -> {
            if (confirmed
                    && !emailInput.getText().isBlank()
                    && !pwInput.getText().isBlank()) {
                configEmail    = emailInput.getText().trim();
                configPassword = pwInput.getText().trim();
                saveConfig();
                if (senderEmailField != null) {
                    senderEmailField.setText(configEmail);
                }
            }
        });
    }

    @FXML
    public void onResetEmailConfig() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Email Settings");
        confirm.setHeaderText("Reset saved email credentials?");
        confirm.setContentText("You will be asked to enter your Gmail and App Password again.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Files.deleteIfExists(CONFIG_PATH);
                    configEmail    = "";
                    configPassword = "";
                    senderEmailField.setText("");
                    showFirstTimeSetup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ── TAB SWITCHING ──────────────────────────────────

    @FXML
    public void switchToEmail() {
        emailPanel.setVisible(true);
        emailPanel.setManaged(true);
        waPanel.setVisible(false);
        waPanel.setManaged(false);
        emailTabBtn.setStyle(TAB_ACTIVE);
        waTabBtn.setStyle(TAB_INACTIVE);
    }

    @FXML
    public void switchToWhatsApp() {
        waPanel.setVisible(true);
        waPanel.setManaged(true);
        emailPanel.setVisible(false);
        emailPanel.setManaged(false);
        waTabBtn.setStyle(TAB_ACTIVE);
        emailTabBtn.setStyle(TAB_INACTIVE);
    }

    // ── EMAIL ATTACHMENTS ──────────────────────────────

    @FXML
    public void onBrowseEmailAttachment() {
        List<File> files = browseFiles(emailAttachmentList.getScene());
        if (files != null) {
            emailAttachments.addAll(files);
            refreshEmailAttachmentList();
        }
    }

    @FXML
    public void onPickFromGalleryEmail() {
        showPhotoGalleryDialog(true); // true = email
    }

    private void refreshEmailAttachmentList() {
        emailAttachmentList.getChildren().clear();
        if (emailAttachments.isEmpty()) {
            emailAttachHint.setText("No files selected");
            return;
        }
        emailAttachHint.setText(emailAttachments.size() + " file(s) selected");
        for (File f : emailAttachments) {
            emailAttachmentList.getChildren().add(
                makeFileTag(f, emailAttachments, this::refreshEmailAttachmentList)
            );
        }
    }

    // ── SEND EMAIL ─────────────────────────────────────

    @FXML
    public void onSendEmail() {
        String from    = senderEmailField.getText().trim();
        String to      = recipientEmailField.getText().trim();
        String subject = emailSubjectField.getText().trim();
        String message = emailMessageArea.getText().trim();

        if (to.isBlank()) {
            setStatus(emailStatusLabel, "❌ Please enter a recipient email.", false);
            return;
        }
        if (from.isBlank()) {
            setStatus(emailStatusLabel, "❌ Sender email not found.", false);
            return;
        }

        // Use saved password — if missing, show setup again
        if (configPassword.isBlank()) {
            showFirstTimeSetup();
            if (configPassword.isBlank()) {
                setStatus(emailStatusLabel, "❌ App password is required.", false);
                return;
            }
        }

        final String finalFrom  = from;
        final String finalAppPw = configPassword;
        setStatus(emailStatusLabel, "⏳ Sending...", true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(finalFrom, finalAppPw);
                    }
                });

                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(finalFrom));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                msg.setSubject(subject.isBlank() ? "Shared from PhotoManager" : subject);

                Multipart multipart = new MimeMultipart();

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(message.isBlank() ?
                    "Photo shared from PhotoManager." : message);
                multipart.addBodyPart(textPart);

                for (File f : emailAttachments) {
                    if (f.exists()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.attachFile(f);
                        multipart.addBodyPart(part);
                    }
                }

                msg.setContent(multipart);
                Transport.send(msg);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
        // SUCCESS POPUP
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Email Sent");
        success.setHeaderText("✅ Email sent successfully!");
        success.setContentText(
            "Your email has been delivered to " + to + ".\n\n" +
            emailAttachments.size() + " attachment(s) included."
        );
        success.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear all inputs after user clicks OK
                recipientEmailField.clear();
                emailSubjectField.setText("Check out my photo!");
                emailMessageArea.setText("Hey! I wanted to share this photo with you 😊");
                emailAttachments.clear();
                refreshEmailAttachmentList();
                setStatus(emailStatusLabel, "", true);
            }
        });
    });

    task.setOnFailed(e -> {
        String errorMsg = task.getException().getMessage();

        // FAIL POPUP
        Alert fail = new Alert(Alert.AlertType.ERROR);
        fail.setTitle("Email Failed");
        fail.setHeaderText("❌ Failed to send email");

        // Give friendly explanation based on error type
        String friendlyMsg;
        if (errorMsg != null && errorMsg.contains("AuthenticationFailed")) {
            friendlyMsg =
                "Authentication failed — your App Password may be incorrect.\n\n" +
                "Click 'Reset ⚙' next to your email to set up a new App Password.";
        } else if (errorMsg != null && errorMsg.contains("timeout")) {
            friendlyMsg = "Connection timed out — please check your internet connection.";
        } else {
            friendlyMsg = "Something went wrong:\n" + errorMsg;
        }

        fail.setContentText(friendlyMsg);

        ButtonType tryAgain = new ButtonType("Try Again", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel   = new ButtonType("Cancel",    ButtonBar.ButtonData.CANCEL_CLOSE);
        fail.getButtonTypes().setAll(tryAgain, cancel);

        fail.showAndWait().ifPresent(response -> {
            if (response == tryAgain) {
                // Re-trigger send
                onSendEmail();
            } else {
                setStatus(emailStatusLabel, "❌ Send cancelled.", false);
            }
        });

        task.getException().printStackTrace();
    });

        new Thread(task).start();
    }

    // ── WHATSAPP PHOTOS ────────────────────────────────

    @FXML
    public void onBrowseWAPhotos() {
        List<File> files = browseFiles(waPhotoList.getScene());
        if (files != null) {
            waPhotos.addAll(files);
            refreshWAPhotoList();
        }
    }

    @FXML
    public void onPickFromGalleryWA() {
        showPhotoGalleryDialog(false); // false = whatsapp
    }

    private void refreshWAPhotoList() {
        waPhotoList.getChildren().clear();
        if (waPhotos.isEmpty()) {
            waPhotoHint.setText("No photos selected");
            return;
        }
        waPhotoHint.setText(waPhotos.size() + " photo(s) selected");
        for (File f : waPhotos) {
            waPhotoList.getChildren().add(
                makeFileTag(f, waPhotos, this::refreshWAPhotoList)
            );
        }
    }

    /**
     * Show a gallery picker dialog with all photos from the library
     * @param isEmail true for email attachments, false for WhatsApp photos
     */
    private void showPhotoGalleryDialog(boolean isEmail) {
        Dialog<List<File>> dialog = new Dialog<>();
        dialog.setTitle("Select Photos");
        dialog.setHeaderText("Select photos from your library");
        dialog.getDialogPane().getButtonTypes().addAll(
            new ButtonType("Add Selected", ButtonBar.ButtonData.OK_DONE),
            ButtonType.CANCEL
        );

        // Create a grid to display photos
        TilePane photoGrid = new TilePane();
        photoGrid.setHgap(10);
        photoGrid.setVgap(10);
        photoGrid.setPrefColumns(4);
        photoGrid.setStyle("-fx-padding: 10;");

        // Keep track of selected files
        List<File> selectedFiles = new ArrayList<>();

        // Load all photos from the library
        List<Path> libraryPhotos = PhotoLibraryService.getLibraryPhotos();

        if (libraryPhotos.isEmpty()) {
            dialog.setHeaderText("No photos in library");
            dialog.setContentText("Your photo library is empty. Import photos first.");
            dialog.showAndWait();
            return;
        }

        for (Path path : libraryPhotos) {
            try {
                File photoFile = path.toFile();
                Image image = new Image(path.toUri().toString());

                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(120);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imageView.setCache(true);

                // Create a selection wrapper
                StackPane photoWrapper = new StackPane(imageView);
                photoWrapper.setPrefSize(120, 120);
                photoWrapper.setStyle("-fx-border-color: #D7BFA7; -fx-border-radius: 8; "
                    + "-fx-border-width: 2; -fx-cursor: hand;");

                // Track selection state
                final boolean[] isSelected = {false};
                Label checkmark = new Label("✓");
                checkmark.setStyle("-fx-font-size: 24px; -fx-text-fill: #2E7D4F; -fx-font-weight: bold;");
                checkmark.setVisible(false);
                StackPane.setAlignment(checkmark, javafx.geometry.Pos.CENTER);
                photoWrapper.getChildren().add(checkmark);

                // Click to select/deselect
                photoWrapper.setOnMouseClicked(event -> {
                    isSelected[0] = !isSelected[0];
                    if (isSelected[0]) {
                        selectedFiles.add(photoFile);
                        photoWrapper.setStyle("-fx-border-color: #2E7D4F; -fx-border-radius: 8; "
                            + "-fx-border-width: 3; -fx-cursor: hand;");
                        checkmark.setVisible(true);
                    } else {
                        selectedFiles.remove(photoFile);
                        photoWrapper.setStyle("-fx-border-color: #D7BFA7; -fx-border-radius: 8; "
                            + "-fx-border-width: 2; -fx-cursor: hand;");
                        checkmark.setVisible(false);
                    }
                });

                photoGrid.getChildren().add(photoWrapper);

            } catch (Exception e) {
                System.err.println("Failed to load photo: " + path);
                e.printStackTrace();
            }
        }

        ScrollPane scrollPane = new ScrollPane(photoGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return selectedFiles;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(files -> {
            if (!files.isEmpty()) {
                if (isEmail) {
                    emailAttachments.addAll(files);
                    refreshEmailAttachmentList();
                } else {
                    waPhotos.addAll(files);
                    refreshWAPhotoList();
                }
            }
        });
    }

    @FXML
    public void onOpenWhatsAppWeb() {
        String message = waMessageArea.getText().trim();
        if (message.isBlank()) message = "Check out this photo! 📸";

        try {
            boolean hasPhotos = !waPhotos.isEmpty();

            // Copy photo files to clipboard
            if (hasPhotos) {
                javafx.scene.input.ClipboardContent content =
                    new javafx.scene.input.ClipboardContent();
                content.putFiles(waPhotos);
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
            }

            // Open WhatsApp Web with pre-filled message
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(
                new URI("https://api.whatsapp.com/send?text=" + encoded)
            );

            // CLEAR POPUP
            Alert clearConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                clearConfirm.setTitle("Clear Selection");
                clearConfirm.setHeaderText("Clear selected photos?");
                clearConfirm.setContentText(
                    "Do you want to clear the selected photos for the next share?"
                );
                ButtonType yes = new ButtonType("Yes, clear", ButtonBar.ButtonData.YES);
                ButtonType no  = new ButtonType("Keep them", ButtonBar.ButtonData.NO);
                clearConfirm.getButtonTypes().setAll(yes, no);
                clearConfirm.showAndWait().ifPresent(r -> {
                    if (r == yes) {
                        waPhotos.clear();
                        refreshWAPhotoList();
                        waMessageArea.setText(
                            "Check out this photo from PhotoManager! 📸"
                        );
                        setStatus(waStatusLabel, "", true);
                    }
                });
        } catch (Exception e) {
            // FAIL POPUP
            Alert fail = new Alert(Alert.AlertType.ERROR);
            fail.setTitle("WhatsApp Failed");
            fail.setHeaderText("❌ Could not open WhatsApp Web");
            fail.setContentText(
                "Something went wrong while opening the browser.\n\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Please make sure you have a default browser set on your computer."
            );
            ButtonType tryAgain = new ButtonType("Try Again", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel   = new ButtonType("Cancel",    ButtonBar.ButtonData.CANCEL_CLOSE);
            fail.getButtonTypes().setAll(tryAgain, cancel);
            fail.showAndWait().ifPresent(response -> {
                if (response == tryAgain) onOpenWhatsAppWeb();
            });
            e.printStackTrace();
        }
    }

    // ── HELPERS ────────────────────────────────────────

    /**
     * Pre-populate an email attachment (called from ImageViewer)
     */
    public void addEmailAttachment(File file) {
        if (file != null && file.exists() && !emailAttachments.contains(file)) {
            emailAttachments.add(file);
            refreshEmailAttachmentList();
        }
    }

    /**
     * Pre-populate a WhatsApp photo (called from ImageViewer)
     */
    public void addWhatsAppPhoto(File file) {
        if (file != null && file.exists() && !waPhotos.contains(file)) {
            waPhotos.add(file);
            refreshWAPhotoList();
        }
    }

    public void setLoggedInEmail(String email) {
        if (senderEmailField != null) senderEmailField.setText(email);
    }

    private List<File> browseFiles(javafx.scene.Scene scene) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Photos");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images",
                "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp")
        );
        return chooser.showOpenMultipleDialog((Stage) scene.getWindow());
    }

    private HBox makeFileTag(File file, List<File> list, Runnable refresh) {
        Label name = new Label(file.getName());
        name.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A3C2E;");

        Button remove = new Button("✕");
        remove.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #B0A090;" +
            "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0 0 0 6;"
        );
        remove.setOnAction(e -> { list.remove(file); refresh.run(); });

        HBox tag = new HBox(6, name, remove);
        tag.setStyle(
            "-fx-background-color: #F5F0EB; -fx-background-radius: 20;" +
            "-fx-border-color: #D7BFA7; -fx-border-radius: 20;" +
            "-fx-padding: 5 12; -fx-alignment: CENTER_LEFT;"
        );
        return tag;
    }

    private void setStatus(Label label, String message, boolean success) {
        label.setText(message);
        label.setStyle(success
            ? "-fx-text-fill: #2E7D4F; -fx-font-weight: bold; -fx-font-size: 13px;"
            : "-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 13px;");
    }
}