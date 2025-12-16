package edu.univ.erp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    public static final String DEFAULT_FILE = "/application.properties";

    private ConfigLoader() {
    }

    public static Properties load() {
        return load(DEFAULT_FILE);
    }

    public static Properties load(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream in = ConfigLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Configuration resource not found: " + resourcePath);
            }
            properties.load(in);
        } catch (IOException e) {
            log.error("Failed to load configuration from {}", resourcePath, e);
            throw new IllegalStateException("Cannot load configuration", e);
        }
        return properties;
    }
}

