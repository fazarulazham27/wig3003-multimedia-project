package com.wig3003.multimedia.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class MosaicGeneratorService {

    private MosaicGeneratorService() {
    }

    public static BufferedImage generateMosaic(
            Path targetPath,
            List<Path> tilePaths,
            int tilesWide,
            int tilePixelSize) throws IOException {

        if (tilesWide < 1 || tilePixelSize < 4) {
            throw new IllegalArgumentException("Invalid mosaic dimensions");
        }

        BufferedImage target = ImageIO.read(targetPath.toFile());
        if (target == null) {
            throw new IOException("Unsupported target image format");
        }

        List<TileInfo> tiles = loadTiles(tilePaths, tilePixelSize);
        if (tiles.isEmpty()) {
            throw new IllegalArgumentException("No valid tile images found");
        }

        int tilesHigh = Math.max(1, (int) Math.round((double) target.getHeight() / target.getWidth() * tilesWide));
        BufferedImage tinyTarget = resize(target, tilesWide, tilesHigh);

        BufferedImage result = new BufferedImage(
                tilesWide * tilePixelSize,
                tilesHigh * tilePixelSize,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();

        try {
            for (int y = 0; y < tilesHigh; y++) {
                for (int x = 0; x < tilesWide; x++) {
                    int rgb = tinyTarget.getRGB(x, y);
                    int tr = (rgb >> 16) & 0xFF;
                    int tg = (rgb >> 8) & 0xFF;
                    int tb = rgb & 0xFF;

                    TileInfo bestTile = findNearestTile(tiles, tr, tg, tb);
                    g2d.drawImage(bestTile.image(), x * tilePixelSize, y * tilePixelSize, null);
                }
            }
        } finally {
            g2d.dispose();
        }

        return result;
    }

    private static List<TileInfo> loadTiles(List<Path> tilePaths, int tileSize) {
        List<TileInfo> tiles = new ArrayList<>();

        for (Path path : tilePaths) {
            try {
                BufferedImage source = ImageIO.read(path.toFile());
                if (source == null) {
                    continue;
                }

                BufferedImage tile = cropCenterSquare(source);
                tile = resize(tile, tileSize, tileSize);

                long sumR = 0;
                long sumG = 0;
                long sumB = 0;
                int count = tile.getWidth() * tile.getHeight();

                for (int y = 0; y < tile.getHeight(); y++) {
                    for (int x = 0; x < tile.getWidth(); x++) {
                        int rgb = tile.getRGB(x, y);
                        sumR += (rgb >> 16) & 0xFF;
                        sumG += (rgb >> 8) & 0xFF;
                        sumB += rgb & 0xFF;
                    }
                }

                tiles.add(new TileInfo(
                        tile,
                        (int) (sumR / count),
                        (int) (sumG / count),
                        (int) (sumB / count)));
            } catch (IOException ignored) {
                // Skip unreadable files.
            }
        }

        return tiles;
    }

    private static TileInfo findNearestTile(List<TileInfo> tiles, int tr, int tg, int tb) {
        TileInfo best = null;
        long bestDistance = Long.MAX_VALUE;

        for (TileInfo tile : tiles) {
            long dr = tr - tile.avgR();
            long dg = tg - tile.avgG();
            long db = tb - tile.avgB();
            long distance = dr * dr + dg * dg + db * db;

            if (distance < bestDistance) {
                bestDistance = distance;
                best = tile;
            }
        }

        return best;
    }

    private static BufferedImage cropCenterSquare(BufferedImage source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        return source.getSubimage(x, y, size, size);
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(source, 0, 0, width, height, null);
        } finally {
            g2d.dispose();
        }
        return resized;
    }

    private record TileInfo(BufferedImage image, int avgR, int avgG, int avgB) {
    }
}
