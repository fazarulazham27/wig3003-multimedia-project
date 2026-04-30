package com.wig3003.multimedia.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class RepositoryController implements SideNavBarController.IFilterableModule {

    @FXML
    private Button importButton;

    @FXML
    private TilePane imagesContainer;

    private List<File> importedFiles;

    private String currentFilter = "All Photos";

    @FXML
    public void initialize() {
        System.out.println("Repository module loaded");

        if (importButton != null) {
            importButton.setOnAction(event -> handleImportImages());
        } else {
            System.out.println("importButton is NULL ❌");
        }
    }

    @FXML
    private void handleImportImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        // List<File> files =
        // fileChooser.showOpenMultipleDialog(importButton.getScene().getWindow());
        importedFiles = fileChooser.showOpenMultipleDialog(importButton.getScene().getWindow());
        List<File> files = importedFiles;

        if (files == null || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            try {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(165);
                imageView.setFitHeight(165);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imageView.setCache(true);

                // Add rounded corners
                Rectangle clip = new Rectangle(165, 165);
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imageView.setClip(clip);

                // Make clickable
                imageView.setOnMouseClicked(event -> showImageViewer(image, file.getAbsolutePath()));

                if (hasAnnotation(file.getAbsolutePath())) {
                    StackPane wrapper = new StackPane();
                    wrapper.setPrefSize(165, 165);

                    wrapper.getChildren().add(imageView);

                    Text heart = new Text("❤");
                    heart.setStyle("-fx-font-size: 20px; -fx-fill: red;");

                    StackPane.setAlignment(heart, Pos.TOP_RIGHT);
                    wrapper.getChildren().add(heart);

                    imagesContainer.getChildren().add(wrapper);

                } else {
                    imagesContainer.getChildren().add(imageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showImageViewer(Image image, String uri) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/image-viewer.fxml"));
            Parent root = loader.load();
            ImageViewerController controller = loader.getController();
            controller.setImage(image, uri);
            Stage stage = new Stage();
            stage.setTitle("Image Viewer");
            stage.setScene(new Scene(root));
            stage.setOnHiding(event -> refreshImages());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasAnnotation(String uri) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(
                System.getProperty("user.home") + "/image_annotations.properties")) {
            props.load(fis);
            String value = props.getProperty(uri);
            return value != null && !value.trim().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private void refreshImages() {
        imagesContainer.getChildren().clear();

        if (importedFiles == null)
            return;

        for (File file : importedFiles) {
            try {
                boolean hasAnno = hasAnnotation(file.getAbsolutePath());

                if ("Annotated".equalsIgnoreCase(currentFilter) && !hasAnno) {
                    continue;
                }

                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(165);
                imageView.setFitHeight(165);

                Rectangle clip = new Rectangle(165, 165);
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imageView.setClip(clip);

                imageView.setOnMouseClicked(event -> showImageViewer(image, file.getAbsolutePath()));

                if (hasAnno) {
                    StackPane wrapper = new StackPane();
                    wrapper.setPrefSize(165, 165);

                    Text heart = new Text("❤");
                    heart.setStyle("-fx-font-size: 20px; -fx-fill: red;");

                    StackPane.setAlignment(heart, Pos.TOP_RIGHT);
                    wrapper.getChildren().addAll(imageView, heart);

                    imagesContainer.getChildren().add(wrapper);
                } else {
                    imagesContainer.getChildren().add(imageView);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void applyFilter(String filter) {
        currentFilter = filter;

        System.out.println("Filter = [" + filter + "]");

        boolean isAllPhotos = filter != null && filter.toLowerCase().contains("all");

        importButton.setVisible(isAllPhotos);
        importButton.setManaged(isAllPhotos);

        refreshImages();
    }
}

