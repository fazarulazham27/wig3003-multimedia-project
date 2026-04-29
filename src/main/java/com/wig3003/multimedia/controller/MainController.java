package com.wig3003.multimedia.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    /**
     * Switch to a module without filter parameter
     */
    public void switchModule(String moduleName) {
        switchModule(moduleName, null);
    }

    /**
     * Switch to a module with optional filter parameter
     * @param moduleName The name of the module to switch to (e.g., "repo", "mosaic", "video", "social")
     * @param filter Optional filter parameter for repository module (e.g., "all", "annotated")
     */
    public void switchModule(String moduleName, String filter) {
        try {
            String fxmlPath = switch (moduleName) {
                case "repo" -> "/fxml/repository-view.fxml";
                case "mosaic" -> "/fxml/mosaic-view.fxml";
                case "video" -> "/fxml/video-view.fxml";
                case "social" -> "/fxml/social-sharing-view.fxml";
                case "dip" -> "/fxml/dip-view.fxml";
                default -> "/fxml/dashboard-view.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) welcomeText.getScene().getWindow();
            Scene newScene = new Scene(loader.load());
            newScene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            // If there's a controller that needs the filter, pass it
            if (filter != null) {
                Object controller = loader.getController();
                if (controller instanceof SideNavBarController.IFilterableModule) {
                    ((SideNavBarController.IFilterableModule) controller).applyFilter(filter);
                }
            }

            stage.setScene(newScene);
            stage.setTitle("PhotoManager - " + moduleName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
