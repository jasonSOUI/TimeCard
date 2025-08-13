## 功能

提供自動登入上下班打卡，可搭配其他排程程式來實現自動打卡

```bash
上班打卡 : java -jar TimeCard-1.0-SNAPSHOT.jar -s on
下班打卡 : java -jar TimeCard-1.0-SNAPSHOT.jar -s off
```

## 環境設定

- JDK17、Maven3
- **瀏覽器驅動程式**：本專案已整合 `WebDriverManager`，會自動下載並管理 Chrome 瀏覽器驅動程式，無需手動下載 `chromedriver.exe`。
- **設定檔**：
    - 程式會依序從以下來源讀取設定：
        1.  **系統環境變數** (例如：`USER_NAME`, `GEMINI_API_KEY`)
        2.  **外部 `timecard.properties` 檔案** (放置於 JAR 檔同目錄下)
        3.  **JAR 內部 `config.properties` 檔案** (僅包含非敏感預設值)
    - **敏感資訊** (如帳號、密碼、API 金鑰) 請務必透過 **系統環境變數** 或 **外部 `timecard.properties` 檔案** 提供。
- [OpenAPI申請](https://platform.openai.com/docs/overview)、[Gemini API申請](https://ai.google.dev/gemini-api/docs/api-key?hl=zh-tw)，選擇一個申請即可

## 如何設定 `timecard.properties`

1.  在專案根目錄或 JAR 檔同目錄下，找到 `timecard.properties.template` 檔案。
2.  將 `timecard.properties.template` 複製一份，並重新命名為 `timecard.properties`。
3.  打開 `timecard.properties` 檔案，填寫您的帳號、密碼和 API 金鑰等資訊。
    ```properties
    # timecard.properties (範例)

    # 您的使用者名稱
    user.name=您的實際帳號

    # 您的密碼
    user.password=您的實際密碼

    # OPEN AI KEY (如果使用 OpenAI 服務)
    api.key=您的 OpenAI API 金鑰

    # Gemini API KEY (如果使用 Gemini 服務)
    gemini.api.key=您的 Gemini API 金鑰

    # 截圖儲存路徑 (可選，如果 config.properties 中沒有設定或需要覆蓋)
    screenshot.path=./screenshots
    ```
4.  確保 `timecard.properties` 檔案與您的 `TimeCard-1.0-SNAPSHOT.jar` 位於相同目錄。

## 畫面
![image](https://github.com/jasonSOUI/TimeCard/blob/master/log.png)

## 流程
![image](https://github.com/jasonSOUI/TimeCard/blob/master/uml.png)
