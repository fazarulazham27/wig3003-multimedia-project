module com.wig3003.multimedia {

    requires javafx.controls;
    requires javafx.fxml;

    opens com.wig3003.multimedia.app to javafx.fxml;
    opens com.wig3003.multimedia.controller to javafx.fxml;

    exports com.wig3003.multimedia.app;
    exports com.wig3003.multimedia.controller;
}