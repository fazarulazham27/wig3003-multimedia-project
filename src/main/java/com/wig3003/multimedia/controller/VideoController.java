package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.FavoritePhotoService;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class VideoController {

    @FXML
    private SideNavBarController sideNavBarController;

    @FXML
    private ListView<Path> favoriteListView;

    @FXML
    private Label favoritesCountLabel;

    @FXML
    private Slider frameDurationSlider;

    @FXML
    private TextArea overlayTextArea;

    @FXML
    private Slider textOpacitySlider;

    @FXML
    private ColorPicker textColorPicker;

    @FXML
    private ComboBox<String> graphicTypeCombo;

    @FXML
    private ColorPicker graphicColorPicker;

    @FXML
    private Slider graphicOpacitySlider;

    @FXML
    private ImageView framePreview;

    @FXML
    private Label textOverlayLabel;

    @FXML
    private StackPane graphicOverlayLayer;

    @FXML
    private StackPane overlayContent;

    @FXML
    private StackPane overlayVisualLayer;

    @FXML
    private Slider seekSlider;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private Label statusLabel;

    private final ObservableList<Path> sourcePhotos = FXCollections.observableArrayList();
    private final List<Image> compiledFrames = new ArrayList<>();

    private Timeline playbackTimeline;
    private double currentSeconds = 0;
    private double frameDurationSeconds = 2.5;
    private double totalDurationSeconds = 0;
    private boolean updatingSeek = false;
    private int favoritesAvailable = 0;
    private double dragStartSceneX;
    private double dragStartSceneY;
    private double dragStartTranslateX;
    private double dragStartTranslateY;

    @FXML
    public void initialize() {
        if (sideNavBarController != null) {
            sideNavBarController.setActiveModuleButton("video");
        }

        favoriteListView.setItems(sourcePhotos);
        favoriteListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        favoriteListView.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFileName().toString());
            }
        });

        graphicTypeCombo.setItems(FXCollections.observableArrayList("None", "Rectangle", "Circle", "Triangle"));
        graphicTypeCombo.getSelectionModel().select("Rectangle");

        overlayTextArea.setText("Write your poem or caption here...");
        textColorPicker.setValue(Color.WHITE);
        graphicColorPicker.setValue(Color.web("#5A3C2E"));

        bindOverlayControls();
        enableOverlayDragging();
        configureSeekSlider();
        onLoadFavoritesToSource();
        setStatus("Add source photos and click Generate Sequence.", false);
    }

    @FXML
    public void onRefreshFavorites() {
        favoritesAvailable = FavoritePhotoService.getFavoritePhotos().size();
        refreshSourceCountLabel();
    }

    @FXML
    public void onLoadFavoritesToSource() {
        List<Path> favorites = FavoritePhotoService.getFavoritePhotos();
        sourcePhotos.setAll(favorites);
        favoritesAvailable = favorites.size();
        refreshSourceCountLabel();

        if (favorites.isEmpty()) {
            setStatus("No favorites yet. Use Add Photos to populate source directly.", false);
        } else {
            setStatus("Loaded " + favorites.size() + " favorite photo(s) into source.", false);
        }
    }

    @FXML
    public void onAddSourcePhotos() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Source Photos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"));

        List<File> files = chooser.showOpenMultipleDialog(favoriteListView.getScene().getWindow());
        if (files == null || files.isEmpty()) {
            return;
        }

        Set<Path> merged = new LinkedHashSet<>(sourcePhotos);
        for (File file : files) {
            merged.add(file.toPath().toAbsolutePath().normalize());
        }

        sourcePhotos.setAll(merged);
        refreshSourceCountLabel();
        setStatus("Added " + files.size() + " photo(s) to video source.", false);
    }

    @FXML
    public void onRemoveSelectedSources() {
        List<Path> selected = new ArrayList<>(favoriteListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            setStatus("Select one or more source photos to remove.", true);
            return;
        }

        sourcePhotos.removeAll(selected);
        refreshSourceCountLabel();
        setStatus("Removed " + selected.size() + " photo(s) from source.", false);
    }

    @FXML
    public void onGenerateSequence() {
        List<Path> selected = new ArrayList<>(favoriteListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            selected.addAll(sourcePhotos);
        }

        if (selected.isEmpty()) {
            setStatus("Video source is empty. Add photos or load favorites first.", true);
            return;
        }

        compiledFrames.clear();
        for (Path path : selected) {
            Image image = new Image(path.toUri().toString(), 0, 0, true, true, true);
            if (!image.isError()) {
                compiledFrames.add(image);
            }
        }

        if (compiledFrames.isEmpty()) {
            setStatus("Selected source photos could not be loaded.", true);
            return;
        }

        frameDurationSeconds = Math.max(0.75, frameDurationSlider.getValue());
        totalDurationSeconds = frameDurationSeconds * compiledFrames.size();
        currentSeconds = 0;

        seekSlider.setMin(0);
        seekSlider.setMax(totalDurationSeconds);
        updateFrameAt(0);
        stopTimeline();

        totalTimeLabel.setText(formatTime(totalDurationSeconds));
        setStatus("Sequence generated with " + compiledFrames.size() + " frames.", false);
    }

    @FXML
    public void onPlay() {
        if (compiledFrames.isEmpty()) {
            setStatus("Generate a sequence first.", true);
            return;
        }

        if (playbackTimeline == null) {
            playbackTimeline = new Timeline(new KeyFrame(Duration.millis(50), event -> advancePlayback(0.05)));
            playbackTimeline.setCycleCount(Animation.INDEFINITE);
        }

        playbackTimeline.play();
        setStatus("Playing generated content.", false);
    }

    @FXML
    public void onPause() {
        if (playbackTimeline != null) {
            playbackTimeline.pause();
        }
        setStatus("Playback paused.", false);
    }

    @FXML
    public void onStop() {
        stopTimeline();
        currentSeconds = 0;
        updateFrameAt(0);
        setStatus("Playback stopped.", false);
    }

    private void bindOverlayControls() {
        overlayTextArea.textProperty().addListener((obs, oldValue, newValue) -> textOverlayLabel.setText(newValue));

        textOpacitySlider.valueProperty().addListener((obs, oldValue, newValue) ->
                textOverlayLabel.setOpacity(newValue.doubleValue()));

        textColorPicker.valueProperty().addListener((obs, oldValue, newValue) ->
                textOverlayLabel.setTextFill(newValue));

        graphicTypeCombo.valueProperty().addListener((obs, oldValue, newValue) -> rebuildGraphicOverlay());
        graphicColorPicker.valueProperty().addListener((obs, oldValue, newValue) -> rebuildGraphicOverlay());
        graphicOpacitySlider.valueProperty().addListener((obs, oldValue, newValue) -> rebuildGraphicOverlay());

        textOverlayLabel.setText(overlayTextArea.getText());
        textOverlayLabel.setOpacity(textOpacitySlider.getValue());
        textOverlayLabel.setTextFill(textColorPicker.getValue());
        textOverlayLabel.setVisible(true);
        textOverlayLabel.setManaged(true);
        rebuildGraphicOverlay();
    }

    private void enableOverlayDragging() {
        overlayContent.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            dragStartSceneX = event.getSceneX();
            dragStartSceneY = event.getSceneY();
            dragStartTranslateX = overlayContent.getTranslateX();
            dragStartTranslateY = overlayContent.getTranslateY();
            event.consume();
        });

        overlayContent.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            double deltaX = event.getSceneX() - dragStartSceneX;
            double deltaY = event.getSceneY() - dragStartSceneY;
            overlayContent.setTranslateX(dragStartTranslateX + deltaX);
            overlayContent.setTranslateY(dragStartTranslateY + deltaY);
            event.consume();
        });
    }

    private void configureSeekSlider() {
        seekSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingSeek && seekSlider.isValueChanging()) {
                updateFrameAt(newValue.doubleValue());
            }
        });

        seekSlider.setOnMouseReleased(event -> {
            if (!updatingSeek) {
                updateFrameAt(seekSlider.getValue());
            }
        });
    }

    private void advancePlayback(double stepSeconds) {
        if (compiledFrames.isEmpty()) {
            return;
        }

        currentSeconds += stepSeconds;
        if (currentSeconds >= totalDurationSeconds) {
            currentSeconds = totalDurationSeconds;
            updateFrameAt(currentSeconds);
            stopTimeline();
            setStatus("Playback completed.", false);
            return;
        }

        updateFrameAt(currentSeconds);
    }

    private void updateFrameAt(double seconds) {
        if (compiledFrames.isEmpty()) {
            framePreview.setImage(null);
            currentTimeLabel.setText("00:00");
            return;
        }

        double clamped = Math.max(0, Math.min(seconds, totalDurationSeconds));
        currentSeconds = clamped;

        int frameIndex = Math.min(
                compiledFrames.size() - 1,
                (int) Math.floor(clamped / frameDurationSeconds));

        framePreview.setImage(compiledFrames.get(frameIndex));
        currentTimeLabel.setText(formatTime(clamped));

        updatingSeek = true;
        seekSlider.setValue(clamped);
        updatingSeek = false;
    }

    private void rebuildGraphicOverlay() {
        overlayVisualLayer.getChildren().clear();

        String type = graphicTypeCombo.getValue();
        if (type == null || "None".equals(type)) {
            return;
        }

        Shape shape = switch (type) {
            case "Circle" -> new Circle(80);
            case "Triangle" -> new Polygon(0, 160, 130, 0, 260, 160);
            default -> new Rectangle(300, 95);
        };

        Color chosen = graphicColorPicker.getValue();
        shape.setFill(chosen);
        shape.setOpacity(graphicOpacitySlider.getValue());
        overlayVisualLayer.getChildren().add(shape);
        StackPane.setAlignment(shape, Pos.CENTER);
    }

    private void stopTimeline() {
        if (playbackTimeline != null) {
            playbackTimeline.stop();
        }
    }

    private String formatTime(double seconds) {
        int total = (int) Math.floor(seconds);
        int mins = total / 60;
        int secs = total % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void refreshSourceCountLabel() {
        favoritesCountLabel.setText("Favorites: " + favoritesAvailable + " | Source: " + sourcePhotos.size());
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        if (error) {
            statusLabel.setStyle("-fx-text-fill: #A63A3A; -fx-font-size: 13px; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #2E7D4F; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
    }
}
