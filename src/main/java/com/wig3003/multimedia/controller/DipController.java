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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class DipController {

    @FXML
    private ImageView imageView;

    @FXML
    private Slider contrastSlider;

    @FXML
    private Slider brightnessSlider;
    @FXML
    private VBox imageContainer;

    private Image originalImage;
    private Image baseImage;
    private Image workingImage;
    
    private String imageUri;

    private boolean grayscale = false;
    private boolean frameOn = false;
    private double resizeScale = 1.0;

    private double rotateAngle = 0;
    private double flipX = 1;
    private double flipY = 1;
    private double translateX = 0;
    private double translateY = 0;

    private FrameStyle frameStyle = FrameStyle.NORMAL;

    private enum FrameStyle {
        NORMAL,
        POLAROID
    }

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {
        contrastSlider.valueProperty().addListener((o, a, b) -> updateImage());
        brightnessSlider.valueProperty().addListener((o, a, b) -> updateImage());
        
        imageView.setPreserveRatio(true);
        clipImageArea();
    }

    public void setImage(Image image, String uri) {
        this.originalImage = image;
        this.workingImage = image;
        this.baseImage = image;
        this.imageUri = uri;

        resetTransforms();
        updateImage();
    }

    // ================= Update Image =================
    private void updateImage() {

        if (baseImage == null) {
            return;
        }

        Image img = ImageProcessor.adjustImage(
                baseImage,
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

        applyTransforms(); // apply zoom or rotate or flip after image update
    }

    private void clipImageArea() {

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.heightProperty().bind(imageContainer.heightProperty());

        imageContainer.setClip(clip);
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
        rotateAngle += 90;
        applyTransforms();
    }

    @FXML
    private void flipHorizontal() {
        flipX *= -1;
        applyTransforms();
    }

    @FXML
    private void flipVertical() {
        flipY *= -1;
        applyTransforms();
    }

    @FXML
    private void increaseSize() {
        resizeScale += 0.1;
        applyTransforms();
    }

    @FXML
    private void decreaseSize() {
        if (resizeScale > 0.2) {
            resizeScale -= 0.1;
        }
        applyTransforms();
    }

    private void applyTransforms() {

        imageView.setFitWidth(500 * resizeScale);
        imageView.setFitHeight(500 * resizeScale);

        imageView.setScaleX(flipX);
        imageView.setScaleY(flipY);

        imageView.setRotate(rotateAngle);

        // move
        imageView.setTranslateX(translateX);
        imageView.setTranslateY(translateY);
    }

    @FXML
    private void moveLeft() {
        translateX = Math.max(-100, translateX - 20);
        applyTransforms();
    }

    @FXML
    private void moveRight() {
        translateX = Math.min(100, translateX + 20);
        applyTransforms();
    }

    @FXML
    private void moveUp() {
        translateY -= 20;
        applyTransforms();
    }

    @FXML
    private void moveDown() {
        translateY += 20;
        applyTransforms();
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

    // ================= SAVE =================
    @FXML
    private void saveEditedImage() {

        try {
            if (imageView.getImage() == null) {
                new Alert(Alert.AlertType.WARNING, "No image to save").showAndWait();
                return;
            }

            // capture everything (brightness, contrast, grayscale, frame, rotation, flip)
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

    // ================= BACK  =================
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

        workingImage = originalImage;
        baseImage = originalImage;

        imageView.setImage(originalImage);

        resetTransforms();
    }

    // ================= RESET =================
    private void resetTransforms() {
        resizeScale = 1.0;
        rotateAngle = 0;
        flipX = 1;
        flipY = 1;
        translateX = 0;
        translateY = 0;

        applyTransforms();
    }
}
