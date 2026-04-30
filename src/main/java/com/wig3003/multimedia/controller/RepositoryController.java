package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.AnnotationService;
import com.wig3003.multimedia.service.FavoritePhotoService;
import com.wig3003.multimedia.service.PhotoLibraryService;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
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

    private String currentFilter = "All Photos";

    @FXML
    public void initialize() {
        if (importButton != null) {
            importButton.setOnAction(event -> handleImportImages());
        }
        refreshImages();
    }

    @FXML
    private void handleImportImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Images");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));

        List<File> files = fileChooser.showOpenMultipleDialog(importButton.getScene().getWindow());
        if (files == null || files.isEmpty()) {
            return;
        }

        List<Path> paths = files.stream().map(File::toPath).toList();
        PhotoLibraryService.addPhotos(paths);
        refreshImages();
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

    private void refreshImages() {
        imagesContainer.getChildren().clear();

        List<Path> library = PhotoLibraryService.getLibraryPhotos();
        for (Path path : library) {
            String absolutePath = path.toAbsolutePath().normalize().toString();
            boolean hasAnnotation = AnnotationService.hasAnnotation(absolutePath);

            if ("Annotated".equalsIgnoreCase(currentFilter) && !hasAnnotation) {
                continue;
            }

            try {
                Image image = new Image(path.toUri().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(165);
                imageView.setFitHeight(165);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imageView.setCache(true);

                Rectangle clip = new Rectangle(165, 165);
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                imageView.setClip(clip);

                imageView.setOnMouseClicked(event -> showImageViewer(image, absolutePath));

                boolean favorite = FavoritePhotoService.isFavorite(absolutePath);
                if (favorite) {
                    StackPane wrapper = new StackPane();
                    wrapper.setPrefSize(165, 165);
                    wrapper.getChildren().add(imageView);

                    Text favoriteTag = new Text("FAV");
                    favoriteTag.setStyle("-fx-font-size: 11px; -fx-fill: #FFFFFF; -fx-font-weight: bold;");
                    StackPane badge = new StackPane(favoriteTag);
                    badge.setStyle("-fx-background-color: #A63A3A; -fx-background-radius: 999; -fx-padding: 4 8;");

                    StackPane.setAlignment(badge, Pos.TOP_RIGHT);
                    StackPane.setMargin(badge, new javafx.geometry.Insets(8));
                    wrapper.getChildren().add(badge);
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

        boolean isAllPhotos = filter != null && filter.toLowerCase().contains("all");
        importButton.setVisible(isAllPhotos);
        importButton.setManaged(isAllPhotos);

        refreshImages();
    }
}
