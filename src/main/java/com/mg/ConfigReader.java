package com.mg;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("找不到 config.properties 檔案");
            }
        } catch (IOException e) {
            throw new RuntimeException("讀取設定檔失敗", e);
        }
    }

    public static String get(String key) {
        String value = System.getenv(key);
        return StringUtils.isNotBlank(value) ? value : properties.getProperty(key);
    }
}
