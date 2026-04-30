/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wig3003.multimedia.service;

/**
 *
 * @author ikhwn
 */
import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class ImageProcessor {

    public static Image adjustImage(Image image, double brightness, double contrast) {

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);

        PixelReader reader = image.getPixelReader();
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

        return Math.max(0, Math.min(1, value));
    }

    public static Image convertToGrayscale(Image image) {

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);

        PixelReader reader = image.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Color color = reader.getColor(x, y);
                double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

                writer.setColor(x, y, Color.gray(gray));
            }
        }

        return output;
    }
}