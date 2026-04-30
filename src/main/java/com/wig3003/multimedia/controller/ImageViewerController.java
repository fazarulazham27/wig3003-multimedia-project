package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

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

    private Image currentImage;
    private String imageUri;

    @FXML
    public void initialize() {
        saveAnnotationButton.setOnAction(event -> saveAnnotation());
        editImageButton.setOnAction(event -> editImage());
        shareImageButton.setOnAction(event -> shareImage());
    }

    public void setImage(Image image, String uri) {
        this.currentImage = image;
        this.imageUri = uri;
        fullImageView.setImage(image);
        loadAnnotation();
    }

    private void loadAnnotation() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(getAnnotationsFile())) {
            props.load(fis);
            String annotation = props.getProperty(imageUri, "");
            annotationTextArea.setText(annotation);
        } catch (IOException e) {
            // File doesn't exist or error, ignore
        }
    }

    private void saveAnnotation() {
        String annotation = annotationTextArea.getText();
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(getAnnotationsFile())) {
            props.load(fis);
        } catch (IOException e) {
            // File doesn't exist, create new
        }
        props.setProperty(imageUri, annotation);
        try (FileOutputStream fos = new FileOutputStream(getAnnotationsFile())) {
            props.store(fos, "Image Annotations");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Annotation saved: " + annotation);
        ((Stage) fullImageView.getScene().getWindow()).close();
        fullImageView.getScene().getWindow().hide();
    }

    private String getAnnotationsFile() {
        return System.getProperty("user.home") + "/image_annotations.properties";
    }

    private void editImage() {
        // TODO: Open image in external editor
        System.out.println("Edit image clicked");
    }

    private void shareImage() {
        // TODO: Implement sharing functionality
        System.out.println("Share image clicked");
    }
}