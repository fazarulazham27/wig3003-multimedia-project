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

public final class PhotoLibraryService {

    private static final Path LIBRARY_PATH = Path.of(
            System.getProperty("user.home"), ".photomanager", "library.properties");

    private PhotoLibraryService() {
    }

    public static List<Path> getLibraryPhotos() {
        Properties props = loadProperties();
        Set<Path> photos = new LinkedHashSet<>();

        for (String key : props.stringPropertyNames()) {
            Path path = Path.of(key).toAbsolutePath().normalize();
            if (Files.exists(path) && Files.isRegularFile(path)) {
                photos.add(path);
            }
        }

        List<Path> ordered = new ArrayList<>(photos);
        ordered.sort(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()));
        return ordered;
    }

    public static void addPhotos(List<Path> paths) {
        if (paths == null || paths.isEmpty()) {
            return;
        }

        Properties props = loadProperties();
        for (Path path : paths) {
            if (path != null) {
                Path normalized = path.toAbsolutePath().normalize();
                if (Files.exists(normalized) && Files.isRegularFile(normalized)) {
                    props.setProperty(normalized.toString(), "true");
                }
            }
        }
        saveProperties(props);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(LIBRARY_PATH)) {
            try (InputStream is = Files.newInputStream(LIBRARY_PATH)) {
                props.load(is);
            } catch (IOException ignored) {
                // Best effort load.
            }
        }
        return props;
    }

    private static void saveProperties(Properties props) {
        try {
            Path parent = LIBRARY_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(LIBRARY_PATH)) {
                props.store(os, "Photo Library");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist photo library", e);
        }
    }
}
