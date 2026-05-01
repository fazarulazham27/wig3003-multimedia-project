package com.wig3003.multimedia.service;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.Queue;

public class ObjectExtractor {

    private static class Point {

        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static Image extractByColor(Image image, int startX, int startY) {

        PixelReader reader = image.getPixelReader();

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        boolean[][] visited = new boolean[width][height];
        Color seedColor = reader.getColor(startX, startY);

        // tolerance (the lower it is = stricter object selection)
        double threshold = 0.25;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {

            Point p = queue.poll();

            Color current = reader.getColor(p.x, p.y);

            writer.setColor(p.x, p.y, current);

            checkNeighbor(queue, visited, reader, seedColor, current,
                    p.x + 1, p.y, width, height, threshold);

            checkNeighbor(queue, visited, reader, seedColor, current,
                    p.x - 1, p.y, width, height, threshold);

            checkNeighbor(queue, visited, reader, seedColor, current,
                    p.x, p.y + 1, width, height, threshold);

            checkNeighbor(queue, visited, reader, seedColor, current,
                    p.x, p.y - 1, width, height, threshold);
        }

        return output;
    }

    private static void checkNeighbor(
            Queue<Point> queue,
            boolean[][] visited,
            PixelReader reader,
            Color seedColor,
            Color currentColor,
            int x, int y,
            int width, int height,
            double threshold
    ) {
        if (x >= 0 && y >= 0 && x < width && y < height && !visited[x][y]) {

            Color neighbor = reader.getColor(x, y);

            boolean closeToSeed
                    = colorMatch(seedColor, neighbor, threshold);

            boolean closeToCurrent
                    = colorMatch(currentColor, neighbor, threshold);

            if (closeToSeed && closeToCurrent) {
                visited[x][y] = true;
                queue.add(new Point(x, y));
            }
        }
    }

    private static boolean colorMatch(Color a, Color b, double threshold) {

        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();

        double diff = Math.sqrt(
                dr * dr
                + dg * dg
                + db * db
        );

        return diff < threshold;
    }
}
