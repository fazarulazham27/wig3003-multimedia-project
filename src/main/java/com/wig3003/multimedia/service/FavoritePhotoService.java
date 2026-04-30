package com.wig3003.multimedia.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class FavoritePhotoService {

    private static final Path FAVORITES_PATH = Path.of(
            System.getProperty("user.home"), ".photomanager", "favorites.properties");

    private FavoritePhotoService() {
    }

    public static boolean isFavorite(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return false;
        }
        return loadProperties().containsKey(normalize(imagePath));
    }

    public static void setFavorite(String imagePath, boolean favorite) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        Properties props = loadProperties();
        String normalized = normalize(imagePath);

        if (favorite) {
            props.setProperty(normalized, "true");
        } else {
            props.remove(normalized);
        }
        saveProperties(props);
    }

    public static List<Path> getFavoritePhotos() {
        Properties props = loadProperties();
        Set<Path> favorites = new LinkedHashSet<>();

        for (String key : props.stringPropertyNames()) {
            Path path = Path.of(key).toAbsolutePath().normalize();
            if (Files.exists(path) && Files.isRegularFile(path)) {
                favorites.add(path);
            }
        }

        List<Path> ordered = new ArrayList<>(favorites);
        ordered.sort(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()));
        return ordered;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(FAVORITES_PATH)) {
            try (InputStream is = Files.newInputStream(FAVORITES_PATH)) {
                props.load(is);
            } catch (IOException ignored) {
                // Best effort load.
            }
        }
        return props;
    }

    private static void saveProperties(Properties props) {
        try {
            Path parent = FAVORITES_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(FAVORITES_PATH)) {
                props.store(os, "Favorite Photos");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist favorites", e);
        }
    }

    private static String normalize(String imagePath) {
        return Path.of(imagePath).toAbsolutePath().normalize().toString();
    }
}
