package com.wig3003.multimedia.service;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public class ImageProcessor {

    // ================= BRIGHTNESS + CONTRAST =================
    public static Image adjustImage(Image image, double brightness, double contrast) {

        if (image == null) {
            return null;
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);

        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            return image;
        }

        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Color color = reader.getColor(x, y);

                double r = process(color.getRed(), brightness, contrast);
                double g = process(color.getGreen(), brightness, contrast);
                double b = process(color.getBlue(), brightness, contrast);

                writer.setColor(x, y, Color.color(r, g, b));
            }
        }

        return output;
    }

    private static double process(double value, double brightness, double contrast) {
        value += brightness;
        value = ((value - 0.5) * (contrast + 1)) + 0.5;

        return clamp(value);
    }

    // ================= GRAYSCALE =================
    public static Image convertToGrayscale(Image image) {

        if (image == null) {
            return null;
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);

        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            return image;
        }

        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Color color = reader.getColor(x, y);

                double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;

                writer.setColor(x, y, Color.gray(gray));
            }
        }

        return output;
    }

    // ================= FRAME =================
    public static Image addFrame(Image image) {
        return addBorder(image, 0.05, Color.WHITE);
    }

    // ================= POLAROID FRAME =================
    public static Image addPolaroidFrame(Image image) {
        return addPolaroid(image, 0.05, Color.WHITE);
    }

    // ================= FRAME CORE =================
    private static Image addBorder(Image image, double percent, Color fxColor) {

        if (image == null) {
            return null;
        }

        BufferedImage src = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);

        int w = src.getWidth();
        int h = src.getHeight();

        int border = (int) Math.round(Math.min(w, h) * percent);

        BufferedImage out = new BufferedImage(
                w + border * 2,
                h + border * 2,
                BufferedImage.TYPE_INT_ARGB
        );

        java.awt.Graphics2D g = out.createGraphics();

        java.awt.Color awtColor = new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue()
        );

        g.setColor(awtColor);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());

        g.drawImage(src, border, border, null);
        g.dispose();

        return javafx.embed.swing.SwingFXUtils.toFXImage(out, null);
    }

    // ================= POLAROID =================
    private static Image addPolaroid(Image image, double percent, Color fxColor) {

        if (image == null) {
            return null;
        }

        BufferedImage src = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);

        int w = src.getWidth();
        int h = src.getHeight();

        int border = (int) Math.round(Math.min(w, h) * percent);

        int bottomExtra = border * 2;

        BufferedImage out = new BufferedImage(
                w + border * 2,
                h + border * 6,
                BufferedImage.TYPE_INT_ARGB
        );

        java.awt.Graphics2D g = out.createGraphics();

        java.awt.Color awtColor = new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue()
        );

        g.setColor(awtColor);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());

        g.drawImage(src, border, border, null);

        g.dispose();

        return javafx.embed.swing.SwingFXUtils.toFXImage(out, null);
    }

    // ================= CLAMP =================
    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    // ================= Resize =================
    public static Image resizeImage(Image image, int width, int height) {

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        return imageView.snapshot(null, null);
    }
}
