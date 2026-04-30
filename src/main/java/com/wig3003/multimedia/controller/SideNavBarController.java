package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;

public class SideNavBarController {

    @FXML private Button allPhotosBtn;
    @FXML private Button annotatedBtn;
    @FXML private Button mosaicBtn;
    @FXML private Button videoBtn;
    @FXML private Button sharePhotosBtn;
    @FXML private Button editImageBtn;

    @FXML
    public void initialize() {
        allPhotosBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setActiveModuleButton("repo", "all");
            }
        });
    }

    /**
     * Navigate to Repository module with "all" filter
     */
    @FXML
    public void onAllPhotosClick() {
        // setActiveButton(allPhotosBtn);
        switchModule("repo", "all");
    }
    
    /**
     * Navigate to Repository module with "annotated" filter
     */
    @FXML
    public void onAnnotatedClick() {
        // setActiveButton(annotatedBtn);
        switchModule("repo", "annotated");
    }

    /**
     * Navigate to Mosaic module
     */
    @FXML
    public void onMosaicClick() {
        // setActiveButton(mosaicBtn);
        switchModule("mosaic", null);
    }

    /**
     * Navigate to Video module
     */
    @FXML
    public void onVideoClick() {
        // setActiveButton(videoBtn);
        switchModule("video", null);
    }

    /**
     * Navigate to Social Sharing module (Share Photos)
     */
    @FXML
    public void onSharePhotosClick() {
        // setActiveButton(sharePhotosBtn);
        switchModule("social", null);
    }
    
    @FXML
    public void onEditImageClick() {
        // setActiveButton(sharePhotosBtn);
        switchModule("dip", null);
    }
    
    private void setActiveButton(Button button) {
        for (Button btn : new Button[]{allPhotosBtn, annotatedBtn, mosaicBtn, videoBtn, sharePhotosBtn,editImageBtn}) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #F2E6D8; -fx-font-weight: normal; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-width: 0; -fx-padding: 12 15; -fx-cursor: hand;");
        }
        button.setStyle("-fx-background-color: #F2E6D8; -fx-text-fill: #6B4B3A; -fx-font-weight: bold; -fx-background-insets: 0; -fx-background-radius: 0; -fx-border-width: 0; -fx-padding: 12 15;");
    }

    /**
     * Switch to a module with optional filter parameter
     */
    private void switchModule(String moduleName, String filter) {
        try {
            String fxmlPath = switch (moduleName) {
                case "repo"   -> "/fxml/repository-view.fxml";
                case "mosaic" -> "/fxml/mosaic-view.fxml";
                case "video"  -> "/fxml/video-view.fxml";
                case "social" -> "/fxml/social-sharing-view.fxml";
                case "dip"    -> "/fxml/dip-view.fxml";
                default       -> "/fxml/repository-view.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Object newModuleRoot = loader.load();
            
            // Get the current root HBox (which has nav bar at index 0, content at index 1)
            javafx.scene.layout.HBox root = (javafx.scene.layout.HBox) allPhotosBtn.getScene().getRoot();
            
            // Apply filter to new module controller if needed
            Object controller = loader.getController();
            if (filter != null && controller instanceof IFilterableModule) {
                ((IFilterableModule) controller).applyFilter(filter);
            }
            
            // Extract center content from the newly loaded module
            // The new module is also an HBox: [nav bar (unused), content area]
            if (newModuleRoot instanceof javafx.scene.layout.HBox newHBox && newHBox.getChildren().size() > 1) {
                javafx.scene.Node newCenter = newHBox.getChildren().get(1);
                newHBox.getChildren().remove(newCenter);
                
                // Replace the center content in the current root (keep nav bar at index 0)
                root.getChildren().set(1, newCenter);
            }
            
            setActiveModuleButton(moduleName, filter);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setActiveModuleButton(String moduleName) {
        setActiveModuleButton(moduleName, null);
    }

    /**
     * Set the active menu item to the given module name
     * This is called when entering a module to highlight the correct nav item
     */
    public void setActiveModuleButton(String moduleName, String filter) {
        Button target = switch (moduleName) {
            case "mosaic" -> mosaicBtn;
            case "video"  -> videoBtn;
            case "social" -> sharePhotosBtn;
            case "dip" -> editImageBtn;
            case "repo"   -> "annotated".equals(filter) ? annotatedBtn : allPhotosBtn;
            default       -> allPhotosBtn;
        };
        setActiveButton(target);
    }

    /**
     * Interface for modules that support filtering
     */
    public interface IFilterableModule {
        void applyFilter(String filter);
    }
}
