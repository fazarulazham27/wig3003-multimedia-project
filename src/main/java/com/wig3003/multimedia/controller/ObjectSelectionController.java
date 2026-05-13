package com.wig3003.multimedia.controller;

import com.wig3003.multimedia.service.PhotoLibraryService;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class ObjectSelectionController {

    @FXML
    private ImageView originalView;
    @FXML
    private ImageView resultView;
    @FXML
    private Canvas overlayCanvas;

    private Image image;
    private String imageUri;

    private GraphicsContext gc;

    // brush data
    private final List<List<double[]>> strokes = new ArrayList<>();
    private List<double[]> currentStroke;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        gc = overlayCanvas.getGraphicsContext2D();

        overlayCanvas.widthProperty().bind(originalView.fitWidthProperty());
        overlayCanvas.heightProperty().bind(originalView.fitHeightProperty());

        overlayCanvas.setOnMousePressed(this::startBrush);
        overlayCanvas.setOnMouseDragged(this::brush);
        overlayCanvas.setOnMouseReleased(this::finishBrush);
    }

    // ================= SET IMAGE =================
    public void setImage(Image image, String uri) {
        this.image = image;
        this.imageUri = uri;

        originalView.setImage(image);
        resultView.setImage(null);

        clearBrush();
    }

    // ================= BRUSH =================
    private void startBrush(MouseEvent e) {

        currentStroke = new ArrayList<>();
        strokes.add(currentStroke);

        addPoint(e);
    }

    private void brush(MouseEvent e) {
        addPoint(e);
    }

    private void addPoint(MouseEvent e) {

        double x = e.getX();
        double y = e.getY();

        currentStroke.add(new double[]{x, y});

        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        gc.strokeLine(x, y, x, y);
    }

    // ================= FINISH =================
    private void finishBrush(MouseEvent e) {

        if (currentStroke == null || currentStroke.isEmpty()) {
            return;
        }

        BufferedImage src = SwingFXUtils.fromFXImage(image, null);
        BufferedImage mask = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        boolean[][] visited = new boolean[src.getWidth()][src.getHeight()];

        // IMPORTANT: convert ALL stroke points as seeds
        for (double[] p : currentStroke) {

            int px = toImageX(p[0]);
            int py = toImageY(p[1]);

            if (px >= 0 && py >= 0 && px < src.getWidth() && py < src.getHeight()) {
                floodFill(src, mask, visited, px, py);
            }
        }

        resultView.setImage(SwingFXUtils.toFXImage(mask, null));
    }

    // ================= FLOOD FILL =================
    private void floodFill(
            BufferedImage src,
            BufferedImage mask,
            boolean[][] visited,
            int startX,
            int startY
    ) {

        int target = src.getRGB(startX, startY);

        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        int w = src.getWidth();
        int h = src.getHeight();

        while (!stack.isEmpty()) {

            int[] p = stack.pop();
            int x = p[0];
            int y = p[1];

            if (x < 0 || y < 0 || x >= w || y >= h) {
                continue;
            }
            if (visited[x][y]) {
                continue;
            }

            int rgb = src.getRGB(x, y);

            if (!similar(target, rgb)) {
                continue;
            }

            visited[x][y] = true;

            mask.setRGB(x, y, rgb);

            stack.push(new int[]{x + 1, y});
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x, y + 1});
            stack.push(new int[]{x, y - 1});
        }
    }

    private boolean similar(int c1, int c2) {

        int r1 = (c1 >> 16) & 0xff;
        int g1 = (c1 >> 8) & 0xff;
        int b1 = c1 & 0xff;

        int r2 = (c2 >> 16) & 0xff;
        int g2 = (c2 >> 8) & 0xff;
        int b2 = c2 & 0xff;

        int threshold = 35;

        return Math.abs(r1 - r2) < threshold
                && Math.abs(g1 - g2) < threshold
                && Math.abs(b1 - b2) < threshold;
    }

    // ================= COORD CONVERSION =================
    private int toImageX(double canvasX) {

        double[] d = getDisplayInfo();
        return (int) ((canvasX - d[2]) * d[4]);
    }

    private int toImageY(double canvasY) {

        double[] d = getDisplayInfo();
        return (int) ((canvasY - d[3]) * d[5]);
    }

    // returns: [imageW, imageH, offsetX, offsetY, scaleX, scaleY]
    private double[] getDisplayInfo() {

        double imgW = image.getWidth();
        double imgH = image.getHeight();

        double viewW = originalView.getFitWidth();
        double viewH = originalView.getFitHeight();

        double imgRatio = imgW / imgH;
        double viewRatio = viewW / viewH;

        double dispW, dispH;

        if (imgRatio > viewRatio) {
            dispW = viewW;
            dispH = viewW / imgRatio;
        } else {
            dispH = viewH;
            dispW = viewH * imgRatio;
        }

        double offsetX = (viewW - dispW) / 2;
        double offsetY = (viewH - dispH) / 2;

        double scaleX = imgW / dispW;
        double scaleY = imgH / dispH;

        return new double[]{dispW, dispH, offsetX, offsetY, scaleX, scaleY};
    }

    // ================= CLEAR =================
    @FXML
    private void clearBrush() {

        strokes.clear();

        if (gc != null) {
            gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        }

        resultView.setImage(null);
    }

    // ================= SAVE =================
    @FXML
    private void saveObject() {

        try {
            Image extracted = resultView.getImage();

            if (extracted == null) {
                new Alert(Alert.AlertType.WARNING, "No object extracted").showAndWait();
                return;
            }

            BufferedImage img = SwingFXUtils.fromFXImage(extracted, null);

            File folder = new File("photo-library");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "object_" + System.currentTimeMillis() + ".png");

            ImageIO.write(img, "png", file);

            PhotoLibraryService.addPhotos(List.of(file.toPath()));

            new Alert(Alert.AlertType.INFORMATION, "Saved").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BACK =================
    @FXML
    private void goBack() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dip-view.fxml")
            );

            Parent root = loader.load();

            DipController controller = loader.getController();
            controller.setImage(image, imageUri);

            Stage stage = (Stage) overlayCanvas.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
