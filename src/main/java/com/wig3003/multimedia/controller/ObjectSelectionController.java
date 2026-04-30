package com.wig3003.multimedia.controller;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;

public class ObjectSelectionController {

    @FXML
    private ImageView originalView;

    @FXML
    private ImageView resultView;

    @FXML
    private Canvas overlayCanvas;

    private Image image;
    private GraphicsContext gc;

    private double lastX, lastY;

    // STROKE STORAGE (UNDO SYSTEM)
    private final List<List<double[]>> strokes = new ArrayList<>();
    private List<double[]> currentStroke;

    @FXML
    public void initialize() {

        image = new Image(
                getClass().getResource("/images/dip-playground.jpg").toExternalForm()
        );

        originalView.setImage(image);

        gc = overlayCanvas.getGraphicsContext2D();

        // IMPORTANT: keep canvas aligned with image
        overlayCanvas.widthProperty().bind(originalView.fitWidthProperty());
        overlayCanvas.heightProperty().bind(originalView.fitHeightProperty());

        overlayCanvas.setOnMousePressed(this::startBrush);
        overlayCanvas.setOnMouseDragged(this::brush);
        overlayCanvas.setOnMouseReleased(this::finishBrush);
    }

    // BRUSH START
    private void startBrush(MouseEvent e) {

        currentStroke = new ArrayList<>();
        strokes.add(currentStroke);

        lastX = e.getX();
        lastY = e.getY();

        currentStroke.add(new double[]{lastX, lastY});
    }

    // BRUSH DRAW
    private void brush(MouseEvent e) {

        double x = e.getX();
        double y = e.getY();

        currentStroke.add(new double[]{x, y});

        gc.setStroke(Color.RED);
        gc.setLineWidth(6);
        gc.strokeLine(lastX, lastY, x, y);

        lastX = x;
        lastY = y;
    }

    // FINISH BRUSH → RUN SELECTION
    private void finishBrush(MouseEvent e) {

        double avgX = 0;
        double avgY = 0;

        for (double[] p : currentStroke) {
            avgX += p[0];
            avgY += p[1];
        }

        avgX /= Math.max(currentStroke.size(), 1);
        avgY /= Math.max(currentStroke.size(), 1);

        extractRegion(avgX, avgY);
    }

    // OBJECT EXTRACTION
    private void extractRegion(double cx, double cy) {

        PixelReader reader = image.getPixelReader();

        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        WritableImage output = new WritableImage(w, h);
        PixelWriter writer = output.getPixelWriter();

        // FIX: coordinate mapping
        double scaleX = image.getWidth() / overlayCanvas.getWidth();
        double scaleY = image.getHeight() / overlayCanvas.getHeight();

        int px = (int) (cx * scaleX);
        int py = (int) (cy * scaleY);

        px = Math.max(0, Math.min(px, w - 1));
        py = Math.max(0, Math.min(py, h - 1));

        Color target = reader.getColor(px, py);

        double threshold = 0.25;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                Color c = reader.getColor(x, y);

                double dr = c.getRed() - target.getRed();
                double dg = c.getGreen() - target.getGreen();
                double db = c.getBlue() - target.getBlue();

                double diff = Math.sqrt(dr * dr + dg * dg + db * db);

                if (diff < threshold) {
                    writer.setColor(x, y, c);
                } else {
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }

        resultView.setImage(output);
    }

    // UNDO LAST STROKE
    @FXML
    private void undoBrush() {

        if (!strokes.isEmpty()) {
            strokes.remove(strokes.size() - 1);
            redrawCanvas();
        }
    }

    // CLEAR ALL BRUSH
    @FXML
    private void clearBrush() {

        strokes.clear();
        redrawCanvas();
    }

    // REDRAW CANVAS
    private void redrawCanvas() {

        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        gc.setStroke(Color.RED);
        gc.setLineWidth(6);

        for (List<double[]> stroke : strokes) {

            for (int i = 1; i < stroke.size(); i++) {

                double[] p1 = stroke.get(i - 1);
                double[] p2 = stroke.get(i);

                gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);
            }
        }
    }

    @FXML
    private void saveObject() throws Exception {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Object Image");

        fileChooser.setInitialFileName("object");

        // Multiple format options
        FileChooser.ExtensionFilter pngFilter
                = new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png");

        FileChooser.ExtensionFilter jpgFilter
                = new FileChooser.ExtensionFilter("JPG Image (*.jpg)", "*.jpg");

        fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {

            WritableImage snapshot = resultView.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            String fileName = file.getName().toLowerCase();

            // Decide format based on extension
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {

                ImageIO.write(bufferedImage, "jpg", file);

            } else {

                // default to PNG
                ImageIO.write(bufferedImage, "png", file);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save Complete");
            alert.setHeaderText(null);
            alert.setContentText("Saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        }
    }
}
