package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.ImageProcessor;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import java.io.File;

public class DipController {

    @FXML
    private ImageView imageView;

    @FXML
    private Slider contrastSlider;

    @FXML
    private Slider brightnessSlider;

    private Image originalImage;
    private Image currentImage;

    // STATES
    private boolean frameApplied = false;
    private boolean polaroidMode = false;
    private boolean grayscaleMode = false;

    @FXML
    public void initialize() {

        imageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(
                        getClass().getResource("/css/dip.css").toExternalForm()
                );
            }
        });

        originalImage = new Image(
                getClass().getResource("/images/dip-fruits.jpg").toExternalForm()
        );

        currentImage = originalImage;
        imageView.setImage(originalImage);

        contrastSlider.valueProperty().addListener((obs, o, n) -> updateImage());
        brightnessSlider.valueProperty().addListener((obs, o, n) -> updateImage());
    }

    // IMAGE PIPELINE
    private void updateImage() {

        Image edited = ImageProcessor.adjustImage(
                originalImage,
                brightnessSlider.getValue(),
                contrastSlider.getValue()
        );

        if (grayscaleMode) {
            edited = ImageProcessor.convertToGrayscale(edited);
        }

        if (frameApplied) {
            if (polaroidMode) {
                edited = addPolaroidFrame(edited, 0.1, java.awt.Color.GRAY);
            } else {
                edited = addFrame(edited, 0.1, java.awt.Color.GRAY);
            }
        }

        currentImage = edited;
        imageView.setImage(edited);
    }

    // NORMAL FRAME
    private Image addFrame(Image image, double percent, java.awt.Color color) {

        BufferedImage src = SwingFXUtils.fromFXImage(image, null);

        int w = src.getWidth();
        int h = src.getHeight();

        int border = (int) Math.round(Math.min(w, h) * percent);

        int newW = w + border * 2;
        int newH = h + border * 2;

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();

        g.setColor(color);
        g.fillRect(0, 0, newW, newH);

        g.drawImage(src, border, border, null);

        g.dispose();

        return SwingFXUtils.toFXImage(out, null);
    }

    // POLAROID FRAME Version
    private Image addPolaroidFrame(Image image, double percent, java.awt.Color color) {

        BufferedImage src = SwingFXUtils.fromFXImage(image, null);

        int w = src.getWidth();
        int h = src.getHeight();

        int border = (int) Math.round(Math.min(w, h) * percent);

        int top = border;
        int left = border;
        int right = border;
        int bottom = border * 2;

        int newW = w + left + right;
        int newH = h + top + bottom;

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();

        g.setColor(color);
        g.fillRect(0, 0, newW, newH);

        int shiftUp = border / 3;

        g.drawImage(src, left, top - shiftUp, null);

        g.dispose();

        return SwingFXUtils.toFXImage(out, null);
    }

    // BUTTON ACTIONS
    @FXML
    private void toggleFrame() {
        frameApplied = !frameApplied;
        updateImage();
    }

    @FXML
    private void togglePolaroid() {
        polaroidMode = !polaroidMode;
        updateImage();
    }

    @FXML
    private void applyGrayscale() {
        grayscaleMode = !grayscaleMode;
        updateImage();
    }

    // REVERT IMAGE
    @FXML
    private void revertImage() {

        frameApplied = false;
        polaroidMode = false;
        grayscaleMode = false;

        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);

        currentImage = originalImage;
        imageView.setImage(originalImage);

        // RESET VISUAL TRANSFORMS
        imageView.setRotate(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
    }

    @FXML
    private void rotateImage() {
        imageView.setRotate(imageView.getRotate() + 90);
    }

    @FXML
    private void flipHorizontal() {
        imageView.setScaleX(imageView.getScaleX() * -1);
    }

    @FXML
    private void flipVertical() {
        imageView.setScaleY(imageView.getScaleY() * -1);
    }

    @FXML
    private void openObjectSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/object-selection-view.fxml")
            );

            javafx.scene.Parent root = loader.load();

            imageView.getScene().setRoot(root);

            System.out.println("Object selection opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveEditedImage() throws Exception {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Edited Image");

        chooser.setInitialFileName("edited-image");

        // PNG + JPG options
        FileChooser.ExtensionFilter pngFilter
                = new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png");

        FileChooser.ExtensionFilter jpgFilter
                = new FileChooser.ExtensionFilter("JPG Image (*.jpg)", "*.jpg");

        chooser.getExtensionFilters().addAll(pngFilter, jpgFilter);

        File file = chooser.showSaveDialog(null);

        if (file != null) {

            WritableImage snapshot = imageView.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            String fileName = file.getName().toLowerCase();

            // Decide format
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {

                ImageIO.write(bufferedImage, "jpg", file);

            } else {

                ImageIO.write(bufferedImage, "png", file);
            }

            // SUCCESS POPUP
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save Complete");
            alert.setHeaderText(null);
            alert.setContentText("Saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        }
    }
}
