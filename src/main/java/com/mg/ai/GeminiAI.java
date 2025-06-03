package com.mg.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.mg.ConfigReader;

import java.util.Base64;
import java.util.List;

public class GeminiAI {

    public String getImageValidateCode(String apiKey, String base64Image) {

        Client client = Client.builder().apiKey(apiKey).build();

        Part imagePart = Part.fromBytes(Base64.getDecoder().decode(base64Image.replaceAll("data:image/png;base64,", "")), "image/png");
        Part textPart = Part.fromText("After analyzing the image, just give me the calculated answer. No explanation needed.");

        Content userMessage = Content.builder()
                .role("user")
                .parts(List.of(textPart, imagePart))
                .build();


        GenerateContentResponse response = client.models.generateContent("gemini-2.0-flash", userMessage, null);
        String answer = response.text();

        answer = answer.replaceAll("[^0-9-]", "");

        return answer;
    }
}
