package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.AnnotationService;
import com.wig3003.multimedia.service.FavoritePhotoService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ImageViewerController {

    @FXML
    private ImageView fullImageView;

    @FXML
    private TextArea annotationTextArea;

    @FXML
    private Button saveAnnotationButton;

    @FXML
    private Button favoriteButton;

    @FXML
    private Button editImageButton;

    @FXML
    private Button shareImageButton;

    private Image currentImage;
    private String imageUri;

    @FXML
    public void initialize() {
        saveAnnotationButton.setOnAction(event -> saveAnnotation());
        favoriteButton.setOnAction(event -> toggleFavorite());
        editImageButton.setOnAction(event -> editImage());
        shareImageButton.setOnAction(event -> shareImage());
    }

    public void setImage(Image image, String uri) {
        this.currentImage = image;
        this.imageUri = uri;
        fullImageView.setImage(image);
        loadAnnotation();
        refreshFavoriteState();
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

    private void toggleFavorite() {
        boolean newState = !FavoritePhotoService.isFavorite(imageUri);
        FavoritePhotoService.setFavorite(imageUri, newState);
        refreshFavoriteState();
    }

    private void refreshFavoriteState() {
        boolean favorite = FavoritePhotoService.isFavorite(imageUri);
        if (favorite) {
            favoriteButton.setText("Remove From Favorites");
            favoriteButton.setStyle(baseActionStyle("#8A2D2D"));
        } else {
            favoriteButton.setText("Add To Favorites");
            favoriteButton.setStyle(baseActionStyle("#5A3C2E"));
        }
    }

    private String baseActionStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-padding: 13; -fx-cursor: hand;";
    }

    private void editImage() {
        System.out.println("Edit image clicked");
    }

    private void shareImage() {
        System.out.println("Share image clicked");
    }
}
