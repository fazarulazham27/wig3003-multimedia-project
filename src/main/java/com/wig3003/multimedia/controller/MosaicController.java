package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.MosaicGeneratorService;
import com.wig3003.multimedia.service.PhotoLibraryService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MosaicController {

    @FXML
    private SideNavBarController sideNavBarController;

    @FXML
    private Label targetImageLabel;

    @FXML
    private Label libraryCountLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Spinner<Integer> tilesWideSpinner;

    @FXML
    private Spinner<Integer> tileSizeSpinner;

    @FXML
    private ImageView mosaicPreview;

    @FXML
    private ProgressIndicator generationProgress;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveButton;

    private Path targetImagePath;
    private BufferedImage generatedMosaic;

    @FXML
    public void initialize() {
        if (sideNavBarController != null) {
            sideNavBarController.setActiveModuleButton("mosaic");
        }
        refreshLibraryCount();
        setStatus("Select a target image and generate your mosaic.", false);
    }

    @FXML
    public void onChooseTargetImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Target Image");
        chooser.getExtensionFilters().add(new ExtensionFilter(
                "Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.webp"));

        File file = chooser.showOpenDialog(targetImageLabel.getScene().getWindow());
        if (file != null) {
            targetImagePath = file.toPath();
            targetImageLabel.setText("Target: " + file.getName());
        }
    }

    @FXML
    public void onRefreshLibrary() {
        refreshLibraryCount();
    }

    @FXML
    public void onGenerateMosaic() {
        List<Path> tilePaths = PhotoLibraryService.getLibraryPhotos();

        if (targetImagePath == null) {
            setStatus("Please choose a target image first.", true);
            return;
        }
        if (tilePaths.isEmpty()) {
            setStatus("Import photos in the repository first. Tiles need your collection.", true);
            return;
        }

        int tilesWide = tilesWideSpinner.getValue();
        int tileSize = tileSizeSpinner.getValue();

        Task<BufferedImage> task = new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                return MosaicGeneratorService.generateMosaic(targetImagePath, tilePaths, tilesWide, tileSize);
            }
        };

        task.setOnRunning(event -> {
            generationProgress.setVisible(true);
            generateButton.setDisable(true);
            saveButton.setDisable(true);
            setStatus("Generating mosaic from " + tilePaths.size() + " tile photos...", false);
        });

        task.setOnSucceeded(event -> {
            generatedMosaic = task.getValue();
            mosaicPreview.setImage(SwingFXUtils.toFXImage(generatedMosaic, null));
            generationProgress.setVisible(false);
            generateButton.setDisable(false);
            saveButton.setDisable(false);
            setStatus("Mosaic generated successfully.", false);
        });

        task.setOnFailed(event -> {
            generationProgress.setVisible(false);
            generateButton.setDisable(false);
            saveButton.setDisable(generatedMosaic == null);
            String message = task.getException() == null
                    ? "Mosaic generation failed."
                    : task.getException().getMessage();
            setStatus(message, true);
        });

        Thread thread = new Thread(task, "mosaic-generator");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void onSaveMosaic() {
        if (generatedMosaic == null) {
            setStatus("Generate a mosaic before saving.", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Mosaic");
        chooser.setInitialFileName("mosaic-output.png");
        chooser.getExtensionFilters().add(new ExtensionFilter("PNG Image", "*.png"));

        File output = chooser.showSaveDialog(mosaicPreview.getScene().getWindow());
        if (output == null) {
            return;
        }

        try {
            ImageIO.write(generatedMosaic, "png", output);
            setStatus("Saved mosaic to " + output.getAbsolutePath(), false);
        } catch (IOException e) {
            setStatus("Failed to save mosaic: " + e.getMessage(), true);
        }
    }

    private void refreshLibraryCount() {
        List<Path> paths = PhotoLibraryService.getLibraryPhotos();
        libraryCountLabel.setText("Tile Library: " + paths.size() + " photos");
    }

    private void setStatus(String message, boolean error) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            if (error) {
                statusLabel.setStyle("-fx-text-fill: #A63A3A; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #2E7D4F; -fx-font-size: 13px; -fx-font-weight: bold;");
            }
        });
    }
}
