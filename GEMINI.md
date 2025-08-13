# 專案改善建議

根據專案結構分析，以下為主要可行的改善方向：

1.  **依賴管理與可攜性** (已調整)：
    *   **問題**：專案原先將 `chromedriver.exe` 硬編碼於專案中，導致專案與特定作業系統綁定，且需手動更新驅動程式，降低可攜性。
    *   **解決方案**：透過在 `pom.xml` 中引入 `WebDriverManager` 依賴，並在程式碼中呼叫 `WebDriverManager.chromedriver().setup()`，實現了瀏覽器驅動程式的自動下載與管理。同時，移除了專案中多餘的 `chromedriver.exe` 檔案，大幅提升了專案的可攜性與維護性。

2.  **設定與密鑰管理** (已調整)：
    *   **問題**：專案原先將敏感資訊（如帳號、密碼、API 金鑰）直接儲存在 `config.properties` 中，若此檔案被提交至版本控制，將導致敏感資訊洩露。此外，對於分發給其他使用者執行的 JAR 檔，直接修改 JAR 內部的設定檔不便，且手動設定環境變數對一般使用者不夠友善。
    *   **解決方案**：
        1.  **分層讀取**：程式將優先從系統環境變數讀取配置，其次從 JAR 檔同目錄下的 `timecard.properties` 外部設定檔讀取，最後才使用 JAR 內部 `config.properties` 的預設值。
        2.  **提供範本**：提供 `timecard.properties.template` 檔案，使用者可複製並填寫敏感資訊後，將其命名為 `timecard.properties` 放置於 JAR 檔旁。
        3.  **移除敏感資訊**：JAR 內部 `config.properties` 將移除所有敏感資訊。

