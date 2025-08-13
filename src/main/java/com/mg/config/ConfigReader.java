package com.mg.config;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties internalProperties = new Properties();
    private static Properties externalProperties = new Properties();
    private final static String INTERNAL_CONFIG_FILE_NAME = "config.properties";
    private final static String EXTERNAL_CONFIG_FILE_NAME = "timecard.properties";

    static {
        // 1. Load internal config.properties (defaults)
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(INTERNAL_CONFIG_FILE_NAME)) {
            if (input != null) {
                internalProperties.load(input);
            } else {
                System.err.println("Warning: Internal config.properties not found on classpath. This might be expected if all configs are external.");
            }
        } catch (IOException e) {
            System.err.println("Error loading internal config.properties: " + e.getMessage());
        }

        // 2. Load external timecard.properties (user-provided)
        try {
            File externalConfigFile = new File(EXTERNAL_CONFIG_FILE_NAME);
            if (externalConfigFile.exists()) {
                try (FileInputStream fis = new FileInputStream(externalConfigFile)) {
                    externalProperties.load(fis);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading external timecard.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        // 1. Try Environment Variables (convert key to ENV_VAR_FORMAT)
        String envKey = key.toUpperCase().replace('.', '_');
        String value = System.getenv(envKey);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        // 2. Try External Properties
        value = externalProperties.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        // 3. Fallback to Internal Properties
        return internalProperties.getProperty(key);
    }
}