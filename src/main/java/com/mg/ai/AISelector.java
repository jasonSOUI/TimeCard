package com.mg.ai;

import com.mg.config.ConfigReader;
import org.apache.commons.lang3.StringUtils;

public class AISelector {

    public static String getImageValidateCode(String base64Image) {

        String openAPIKey = ConfigReader.get("api.key");
        String geminiAPIKey = ConfigReader.get("gemini.api.key");

        if (StringUtils.isNotBlank(openAPIKey)) {
            return new OpenAI().getImageValidateCode(openAPIKey, base64Image);
        }

        if (StringUtils.isNotBlank(geminiAPIKey)) {
            return new GeminiAI().getImageValidateCode(geminiAPIKey, base64Image);
        }

        throw new RuntimeException("尚未設定任何API KEY!!!");
    }

}