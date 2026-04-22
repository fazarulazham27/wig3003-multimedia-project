package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class SideNavBarController {

    @FXML private Button allPhotosBtn;
    @FXML private Button annotatedBtn;
    @FXML private Button mosaicBtn;
    @FXML private Button videoBtn;
    @FXML private Button sharePhotosBtn;

    private Button activeButton;
    private MainController mainController;

    @FXML
    public void initialize() {
        // FIX 1: active button - Set "Share Photos" as active by default (for social module)
        // Other modules will have this called fresh, so we always start with Share Photos active
        setActiveButton(sharePhotosBtn);
    }

    /**
     * Navigate to Repository module with "all" filter
     */
    @FXML
    public void onAllPhotosClick() {
        setActiveButton(allPhotosBtn);
        switchModule("repo", "all");
    }

    /**
     * Navigate to Repository module with "annotated" filter
     */
    @FXML
    public void onAnnotatedClick() {
        setActiveButton(annotatedBtn);
        switchModule("repo", "annotated");
    }

    /**
     * Navigate to Mosaic module
     */
    @FXML
    public void onMosaicClick() {
        setActiveButton(mosaicBtn);
        switchModule("mosaic");
    }

    /**
     * Navigate to Video module
     */
    @FXML
    public void onVideoClick() {
        setActiveButton(videoBtn);
        switchModule("video");
    }

    /**
     * Navigate to Social Sharing module (Share Photos)
     */
    @FXML
    public void onSharePhotosClick() {
        setActiveButton(sharePhotosBtn);
        switchModule("social");
    }

    /**
     * Update the active button styling
     */
    private void setActiveButton(Button button) {
        // FIX 1: active button - Properly deactivate all buttons first
        if (allPhotosBtn != null) {
            allPhotosBtn.getStyleClass().removeAll("nav-item", "nav-item-active");
            allPhotosBtn.getStyleClass().add("nav-item");
        }
        if (annotatedBtn != null) {
            annotatedBtn.getStyleClass().removeAll("nav-item", "nav-item-active");
            annotatedBtn.getStyleClass().add("nav-item");
        }
        if (mosaicBtn != null) {
            mosaicBtn.getStyleClass().removeAll("nav-item", "nav-item-active");
            mosaicBtn.getStyleClass().add("nav-item");
        }
        if (videoBtn != null) {
            videoBtn.getStyleClass().removeAll("nav-item", "nav-item-active");
            videoBtn.getStyleClass().add("nav-item");
        }
        if (sharePhotosBtn != null) {
            sharePhotosBtn.getStyleClass().removeAll("nav-item", "nav-item-active");
            sharePhotosBtn.getStyleClass().add("nav-item");
        }
        
        // FIX 1: active button - Set the clicked button as active
        button.getStyleClass().removeAll("nav-item", "nav-item-active");
        button.getStyleClass().add("nav-item-active");
        activeButton = button;
    }

    /**
     * Switch to a module without filter parameter
     */
    private void switchModule(String moduleName) {
        switchModule(moduleName, null);
    }

    /**
     * Switch to a module with optional filter parameter
     */
    private void switchModule(String moduleName, String filter) {
        try {
            String fxmlPath = switch (moduleName) {
                case "repo" -> "/fxml/repository-view.fxml";
                case "mosaic" -> "/fxml/mosaic-view.fxml";
                case "video" -> "/fxml/video-view.fxml";
                case "social" -> "/fxml/social-sharing-view.fxml";
                default -> "/fxml/dashboard-view.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) allPhotosBtn.getScene().getWindow();
            Scene newScene = new Scene(loader.load());
            newScene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            // If there's a controller that needs the filter, pass it
            if (filter != null) {
                Object controller = loader.getController();
                if (controller instanceof IFilterableModule) {
                    ((IFilterableModule) controller).applyFilter(filter);
                }
            }

            stage.setScene(newScene);
            stage.setTitle("PhotoManager - " + moduleName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the active menu item to the given module name
     * This is called when entering a module to highlight the correct nav item
     */
    public void setActiveModule(String moduleName) {
        switch (moduleName) {
            case "repo" -> setActiveButton(allPhotosBtn);
            case "mosaic" -> setActiveButton(mosaicBtn);
            case "video" -> setActiveButton(videoBtn);
            case "social" -> setActiveButton(sharePhotosBtn);
            default -> setActiveButton(sharePhotosBtn);
        }
    }

    /**
     * Interface for modules that support filtering
     */
    public interface IFilterableModule {
        void applyFilter(String filter);
    }
}
