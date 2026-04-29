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
        double threshold = 0.20;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {

            Point p = queue.poll();

            Color current = reader.getColor(p.x, p.y);

            if (colorMatch(seedColor, current, threshold)) {
                writer.setColor(p.x, p.y, current);

                // 4-direction flood fill
                addNeighbor(queue, visited, p.x + 1, p.y, width, height);
                addNeighbor(queue, visited, p.x - 1, p.y, width, height);
                addNeighbor(queue, visited, p.x, p.y + 1, width, height);
                addNeighbor(queue, visited, p.x, p.y - 1, width, height);

            } else {
                writer.setColor(p.x, p.y, Color.TRANSPARENT);
            }
        }

        return output;
    }

    private static void addNeighbor(Queue<Point> queue,
                                    boolean[][] visited,
                                    int x, int y,
                                    int width, int height) {

        if (x >= 0 && y >= 0 && x < width && y < height && !visited[x][y]) {
            visited[x][y] = true;
            queue.add(new Point(x, y));
        }
    }

    private static boolean colorMatch(Color a, Color b, double threshold) {

        double diff =
                Math.abs(a.getRed() - b.getRed()) +
                Math.abs(a.getGreen() - b.getGreen()) +
                Math.abs(a.getBlue() - b.getBlue());

        return diff < threshold;
    }
}