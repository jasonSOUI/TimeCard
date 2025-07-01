package com.mg.service;

import com.mg.ai.AISelector;
import com.mg.config.ConfigReader;
import com.mg.enums.TimeCardStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 自動打卡服務
 */
public class AutoTimeCardService {

    /**
     * 打卡
     * @param status
     * @param debug
     */
    public void checkin(TimeCardStatus status, boolean debug) {

        WebDriver driver = null;

        try {

            // 取得打卡狀態
            String webDriverPath =  ConfigReader.get("webdriver.path");
            String chromePath =  ConfigReader.get("chrome.path");

            // 指定 ChromeDriver 路徑（若 chromedriver.exe 不在環境變數）
            System.setProperty("webdriver.chrome.driver", webDriverPath);

            // 可選：手動指定 Chrome 安裝路徑（若未裝在預設位置）
            ChromeOptions options = new ChromeOptions();
            options.setBinary(chromePath);
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-debugging-port=9222");

            if(debug) {
                options.addArguments("--headless");
            }

            // 建立 Driver 實例
            driver = new ChromeDriver(options);

            // 設定全域等待時間
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // 登入
            login(driver);
            System.out.println("login success!!");

            // 打卡
            checkin(driver, status);
            System.out.println("checkin success!!");

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if(Objects.nonNull(driver)) {
                driver.quit(); // 關閉瀏覽器
            }
        }
    }

    /**
     * 登入
     * @param driver
     * @throws Exception
     */
    private void login(WebDriver driver) throws Exception {

        String loginUrl = ConfigReader.get("login.url");
        String userName =  ConfigReader.get("user.name");
        String userPassword =  ConfigReader.get("user.password");

        // 開啟網頁
        driver.get(loginUrl);

        // 帳號/密碼
        List<WebElement> list = driver.findElements(By.className("el-input__inner"));
        list.get(0).sendKeys(userName);
        list.get(1).sendKeys(userPassword);

        // 驗證碼圖片
        WebElement loginCode = driver.findElement(By.className("login-code"));
        WebElement img = loginCode.findElement(By.tagName("img"));
        String captchaUrl = img.getAttribute("src");
        String code = AISelector.getImageValidateCode(captchaUrl);
        System.out.println("驗證碼圖片答案:" + code);
        list.get(2).sendKeys(code);

        // 登入按鈕
        driver.findElement(By.tagName("button")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("checkin-btn-row")));
    }

    /**
     * 打卡
     * @param driver
     * @param status
     */
    private static void checkin(WebDriver driver, TimeCardStatus status) throws IOException {

        boolean isCheckIn = false;

        // 下單打卡按鈕
        List<WebElement> buttons =  driver.findElement(By.className("checkin-btn-row")).findElements(By.className("checkin-btn"));
        Optional<WebElement> onButton = buttons.stream().filter(e -> "上班打卡".equals(e.getText().trim())).findFirst();
        Optional<WebElement> offButton = buttons.stream().filter(e -> "下班打卡".equals(e.getText().trim())).findFirst();

        // 上班打卡
        if(status.isOn()) {
            if (StringUtils.isNotBlank(onButton.get().getAttribute("disabled")) && "true".equals(onButton.get().getAttribute("disabled"))) {
                System.out.println("已執行過上班打卡!!!");
            } else {
                isCheckIn = true;
                onButton.get().click();
                System.out.println("執行上班打卡:" + getDateTime());
            }
        }

        // 下班打卡
        if (status.isOff()) {
            if (StringUtils.isNotBlank(offButton.get().getAttribute("disabled")) && "true".equals(onButton.get().getAttribute("disabled"))) {
                System.out.println("已執行過下班打卡!!!");
            } else {
                isCheckIn = true;
                offButton.get().click();
                System.out.println("執行下班打卡:" + getDateTime());
            }
        }

        // 確認視窗
        if(isCheckIn) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".el-message-box__btns .el-button--primary")));
            confirmButton.click();
            // 等待確認視窗消失
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("el-message-box__wrapper")));
        }

        // 截圖
        screenshot(driver);
    }

    /**
     * 截圖記錄
     * @param driver
     */
    private static void screenshot(WebDriver driver) {
        try {

            String path = ConfigReader.get("screenshot.path");
            if (StringUtils.isNotBlank(path)) {
                File filePath = new File(path);
                if (!filePath.exists()) filePath.mkdirs();
            } else {
                return;
            }

            WebElement divElement = driver.findElement(By.className("checkin-main"));

            if(divElement != null) {
                Point point = divElement.getLocation();
                int x = point.getX();
                int y = point.getY();
                Rectangle rect = new Rectangle(x, y, 480, 420);

                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                BufferedImage fullScreenImage = ImageIO.read(screenshot);

                // 裁剪圖片
                BufferedImage croppedImage = fullScreenImage.getSubimage(
                        rect.x,
                        rect.y,
                        rect.width,
                        rect.height
                );

                // 將裁剪後的圖片保存到指定路徑
                File output = new File(path + File.separator + String.format("%s.png", getSimpleDateTime()));
                ImageIO.write(croppedImage, "png", output);
                System.out.println("成功截圖指定 DIV 並保存到: " + output.getAbsolutePath());

            } else {

                TakesScreenshot screenshot = (TakesScreenshot) driver;
                File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);
                File destFile = new File(path + File.separator + String.format("%s.png", getSimpleDateTime()));
                FileUtils.copyFile(sourceFile, destFile);
            }

        } catch(Exception ex) {
            System.out.println("screenshot fail : " + ex.getMessage());
        }
    }

    private static String getSimpleDateTime() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }


    private static String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    private static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

}
