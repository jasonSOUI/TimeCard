package com.mg;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {


    private static Properties properties = new Properties();

    private final static String CONFIG_FILE_NAME = "config.properties";

    static {

        try {
            File currentDirConfigFile = new File(CONFIG_FILE_NAME);
            if(currentDirConfigFile.exists()) {
                properties.load(new FileInputStream(currentDirConfigFile));
            } else {
                try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                    if (input != null) {
                        properties.load(input);
                    } else {
                        throw new RuntimeException("找不到 config.properties 檔案");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("讀取設定檔失敗", e);
                }
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
