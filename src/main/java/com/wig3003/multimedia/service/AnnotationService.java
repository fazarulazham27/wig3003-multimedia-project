package com.wig3003.multimedia.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AnnotationService {

    private static final Path ANNOTATIONS_PATH = Path.of(
            System.getProperty("user.home"), "image_annotations.properties");

    private AnnotationService() {
    }

    public static String getAnnotation(String imagePath) {
        Properties props = loadProperties();
        return props.getProperty(normalize(imagePath), "");
    }

    public static boolean hasAnnotation(String imagePath) {
        return !getAnnotation(imagePath).isBlank();
    }

    public static void saveAnnotation(String imagePath, String annotation) {
        Properties props = loadProperties();
        props.setProperty(normalize(imagePath), annotation == null ? "" : annotation.trim());
        saveProperties(props);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(ANNOTATIONS_PATH)) {
            try (InputStream is = Files.newInputStream(ANNOTATIONS_PATH)) {
                props.load(is);
            } catch (IOException ignored) {
                // Fail silently so the UI remains responsive.
            }
        }
        return props;
    }

    private static void saveProperties(Properties props) {
        try {
            Path parent = ANNOTATIONS_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(ANNOTATIONS_PATH)) {
                props.store(os, "Image Annotations");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save annotations", e);
        }
    }

    private static String normalize(String imagePath) {
        return Path.of(imagePath).toAbsolutePath().normalize().toString();
    }
}
