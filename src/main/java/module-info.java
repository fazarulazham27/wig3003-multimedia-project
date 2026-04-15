module javafx.multimediaproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens javafx.multimediaproject to javafx.fxml;
    exports javafx.multimediaproject;
}