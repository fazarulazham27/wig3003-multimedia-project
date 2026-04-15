package com.wig3003.multimedia.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/hello-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 100, 100);

        stage.setTitle("Multimedia Project");
        stage.setScene(scene);

        stage.setMinWidth(300);
        stage.setMinHeight(300);

        stage.show();
    }
}
