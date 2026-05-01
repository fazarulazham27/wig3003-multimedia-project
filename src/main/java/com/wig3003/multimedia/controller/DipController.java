package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.ImageProcessor;
import com.wig3003.multimedia.service.PhotoLibraryService;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class DipController {

    @FXML
    private ImageView imageView;

    @FXML
    private Slider contrastSlider;

    @FXML
    private Slider brightnessSlider;

    private Image originalImage;
    private Image workingImage;
    private String imageUri;

    private boolean grayscale = false;
    private boolean frameOn = false;

    private FrameStyle frameStyle = FrameStyle.NORMAL;

    private enum FrameStyle {
        NORMAL,
        POLAROID
    }

    // ================= INIT =================
    @FXML
    public void initialize() {
        contrastSlider.valueProperty().addListener((o, a, b) -> updateImage());
        brightnessSlider.valueProperty().addListener((o, a, b) -> updateImage());
    }

    public void setImage(Image image, String uri) {
        this.originalImage = image;
        this.workingImage = image;
        this.imageUri = uri;

        resetControls();
        updateImage();
    }

    // ================= PIPELINE =================
    private void updateImage() {
        if (originalImage == null) {
            return;
        }

        Image img = ImageProcessor.adjustImage(
                originalImage,
                brightnessSlider.getValue(),
                contrastSlider.getValue()
        );

        if (grayscale) {
            img = ImageProcessor.convertToGrayscale(img);
        }

        if (frameOn) {
            img = (frameStyle == FrameStyle.POLAROID)
                    ? ImageProcessor.addPolaroidFrame(img)
                    : ImageProcessor.addFrame(img);
        }

        workingImage = img;
        imageView.setImage(img);
    }

    // ================= GRAYSCALE =================
    @FXML
    private void applyGrayscale() {
        grayscale = !grayscale;
        updateImage();
    }

    // ================= FRAME =================
    @FXML
    private void toggleFrame() {
        frameOn = !frameOn;
        updateImage();
    }

    @FXML
    private void changeFrameStyle() {
        frameStyle = (frameStyle == FrameStyle.NORMAL)
                ? FrameStyle.POLAROID
                : FrameStyle.NORMAL;

        updateImage();
    }

    // ================= TRANSFORMS =================
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

    // ================= OBJECT =================
    @FXML
    private void openObjectSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/object-selection-view.fxml")
            );

            Parent root = loader.load();

            ObjectSelectionController controller = loader.getController();
            controller.setImage(workingImage, imageUri);

            Stage stage = (Stage) imageView.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SAVE (FIXED) =================
    @FXML
    private void saveEditedImage() {

        try {
            if (imageView.getImage() == null) {
                new Alert(Alert.AlertType.WARNING, "No image to save").showAndWait();
                return;
            }

            // capture EVERYTHING (brightness, contrast, grayscale, frame, rotation, flip)
            Image snapshot = imageView.snapshot(null, null);

            BufferedImage img = SwingFXUtils.fromFXImage(snapshot, null);

            File folder = new File("photo-library");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "edited_" + System.currentTimeMillis() + ".png");

            ImageIO.write(img, "png", file);

            PhotoLibraryService.addPhotos(List.of(file.toPath()));

            new Alert(Alert.AlertType.INFORMATION, "Image saved successfully").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BACK (FIXED) =================
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/image-viewer.fxml")
            );

            Parent root = loader.load();

            ImageViewerController controller = loader.getController();
            controller.setImage(workingImage, imageUri);

            Stage stage = (Stage) imageView.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REVERT =================
    @FXML
    private void revertImage() {

        grayscale = false;
        frameOn = false;
        frameStyle = FrameStyle.NORMAL;

        contrastSlider.setValue(0);
        brightnessSlider.setValue(0);

        imageView.setRotate(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);

        workingImage = originalImage;
        imageView.setImage(originalImage);
    }

    // ================= RESET =================
    private void resetControls() {
        grayscale = false;
        frameOn = false;
        frameStyle = FrameStyle.NORMAL;

        contrastSlider.setValue(0);
        brightnessSlider.setValue(0);

        imageView.setRotate(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
    }
}
