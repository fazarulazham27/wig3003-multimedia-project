package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.app.MainApp;
import com.wig3003.multimedia.service.AnnotationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import com.wig3003.multimedia.service.PhotoLibraryService;
import java.io.File;
import java.nio.file.Path;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ImageViewerController {

    @FXML
    private ImageView fullImageView;

    @FXML
    private TextArea annotationTextArea;

    @FXML
    private Button saveAnnotationButton;

    @FXML
    private Button editImageButton;

    @FXML
    private Button shareImageButton;

    @FXML
    private Button deleteImageButton;

    private Image currentImage;
    private String imageUri;

    @FXML
    public void initialize() {
        saveAnnotationButton.setOnAction(event -> saveAnnotation());
        // favoriteButton.setOnAction(event -> toggleFavorite());
        editImageButton.setOnAction(event -> {
            System.out.println("EDIT CLICK REGISTERED");
            editImage();
        });
        shareImageButton.setOnAction(event -> shareImage());
        deleteImageButton.setOnAction(event -> deleteImage());
    }

    public void setImage(Image image, String uri) {
        this.currentImage = image;
        this.imageUri = uri;
        fullImageView.setImage(image);
        loadAnnotation();
        // refreshFavoriteState();
    }

    private void loadAnnotation() {
        annotationTextArea.setText(AnnotationService.getAnnotation(imageUri));
    }

    private void saveAnnotation() {
        try {
            AnnotationService.saveAnnotation(imageUri, annotationTextArea.getText());
            ((Stage) fullImageView.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String baseActionStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-padding: 13; -fx-cursor: hand;";
    }

    private void editImage() {
    try {
        if (currentImage == null) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/dip-view.fxml")
        );

        Parent root = loader.load();

        DipController controller = loader.getController();
        controller.setImage(currentImage, imageUri);

        // use SAME window instead of new Stage
        Stage stage = (Stage) fullImageView.getScene().getWindow();
        stage.getScene().setRoot(root);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void shareImage() {
        try {
            if (imageUri == null || imageUri.isBlank()) return;

            Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
            choiceDialog.setTitle("Share Photo");
            choiceDialog.setHeaderText("Choose sharing method");
            choiceDialog.setContentText("How would you like to share this photo?");

            ButtonType emailBtn    = new ButtonType("📧 Email");
            ButtonType whatsappBtn = new ButtonType("💬 WhatsApp");
            ButtonType cancelBtn   = ButtonType.CANCEL;
            choiceDialog.getButtonTypes().setAll(emailBtn, whatsappBtn, cancelBtn);

            var result = choiceDialog.showAndWait();
            if (result.isEmpty() || result.get() == cancelBtn) return;

            File imageFile = Path.of(imageUri).toFile();

            if (result.get() == emailBtn) {
                SocialSharingController.setPendingShareFile(imageFile);
                SocialSharingController.setPendingTab("email");
            } else {
                SocialSharingController.setPendingShareFile(imageFile);
                SocialSharingController.setPendingTab("whatsapp");
            }
            
            Stage mainStage = MainApp.getInstance().getMainStage();
            if (mainStage == null) {
                System.err.println("Could not access main stage");
                return;
            }

            javafx.scene.layout.HBox mainRoot =
                (javafx.scene.layout.HBox) mainStage.getScene().getRoot();

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/social-sharing-view.fxml")
            );
            Object newModuleRoot = loader.load();

            if (newModuleRoot instanceof javafx.scene.layout.HBox newHBox
                    && newHBox.getChildren().size() > 1) {
                javafx.scene.Node newCenter = newHBox.getChildren().get(1);
                newHBox.getChildren().remove(newCenter);
                mainRoot.getChildren().set(1, newCenter);
            }

            SideNavBarController nav = SideNavBarController.getInstance();
            if (nav != null) nav.setActiveModuleButton("social");

            Stage imageViewerStage = (Stage) fullImageView.getScene().getWindow();
            imageViewerStage.close();

        } catch (Exception e) {
            System.err.println("Failed to share image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteImage() {
        try {
            PhotoLibraryService.deletePhoto(Path.of(imageUri));
            ((Stage) fullImageView.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
