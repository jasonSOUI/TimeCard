package com.mg.config;

import com.mg.enums.TimeCardStatus;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String SCHEDULE_CONFIG_FILE = "schedule.properties";
    private static final Properties properties = new Properties();

    static {
        File configFile = new File(SCHEDULE_CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
            } catch (IOException ex) {
                System.err.println("Error loading schedule.properties: " + ex.getMessage());
            }
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void save() {
        try (OutputStream output = new FileOutputStream(SCHEDULE_CONFIG_FILE)) {
            properties.store(output, "Schedule Settings");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // --- Helper methods for schedule settings ---

    public static boolean isScheduleEnabled(TimeCardStatus status) {
        return Boolean.parseBoolean(getProperty("schedule." + status.name().toLowerCase() + ".enabled", "false"));
    }

    public static int getScheduleHour(TimeCardStatus status) {
        int defaultHour = status == TimeCardStatus.ON ? 9 : 18;
        return Integer.parseInt(getProperty("schedule." + status.name().toLowerCase() + ".hour", String.valueOf(defaultHour)));
    }

    public static int getScheduleMinute(TimeCardStatus status) {
        return Integer.parseInt(getProperty("schedule." + status.name().toLowerCase() + ".minute", "0"));
    }
}
