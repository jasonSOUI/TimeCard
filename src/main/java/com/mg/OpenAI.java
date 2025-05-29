package com.mg;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.List;

public class OpenAI {

    /**
     * 解析圖片取得驗證碼
     * @param base64Image
     * @return
     */
    public static String getImageValidateCode(String base64Image) {

        String apiKey = ConfigReader.get("api.key");
        OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

        ChatCompletionContentPart logoContentPart =
                ChatCompletionContentPart.ofImageUrl(ChatCompletionContentPartImage.builder()
                        .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder().url(base64Image).build()).build());

        ChatCompletionContentPart questionContentPart =
                ChatCompletionContentPart.ofText(ChatCompletionContentPartText.builder()
                        .text("After analyzing the image, just give me the calculated answer. No explanation needed.")
                        .build());

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .maxCompletionTokens(2048)
                .addUserMessageOfArrayOfContentParts(List.of(questionContentPart, logoContentPart))
                .build();

        String answer = client.chat().completions().create(createParams).choices().stream()
                .flatMap(choice -> choice.message().content().stream())
                .findFirst().get();

        // 避免出現非答案文字
        answer = answer.replaceAll("[^0-9-]", "");

        return answer;
    }
}
