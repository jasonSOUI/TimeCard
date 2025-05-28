package com.mg;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 自動登入打卡
 */
public class App {

    public static void main(String[] args) {

        WebDriver driver = null;

        try {

            // 取得打卡狀態
            Pair<TimeCardStatus, Boolean> params = getCheckinStatus(args);
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

            if(!params.getRight()) {
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
            checkin(driver, params.getLeft());
            System.out.println("checkin success!!");

            // 取得打卡紀錄
            String record = getTimeCardLog(driver);
            System.out.println("Get TimeCard Log:" + record);

            Thread.sleep(3000);

        } catch (Exception ex) {
            ex.printStackTrace();
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
    private static void login(WebDriver driver) throws Exception {

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
        String code = OpenAI.getImageValidateCode(captchaUrl);
        System.out.println("驗證碼圖片答案:" + code);
        list.get(2).sendKeys(code);

        // 登入按鈕
        driver.findElement(By.tagName("button")).click();
        Thread.sleep(3000);
    }

    /**
     * 打卡
     * @param driver
     * @param status
     */
    private static void checkin(WebDriver driver, TimeCardStatus status) {

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
            WebElement confirmButton = driver.findElement(By.className("el-message-box__btns")).findElement(By.className("el-button--primary"));
            confirmButton.click();
        }
    }

    /**
     * 取得打卡紀錄
     * @param driver
     * @return
     */
    private static String getTimeCardLog(WebDriver driver) {

        String timeCardUrl = ConfigReader.get("time.card.url");

        // 導向打卡紀錄
        driver.get(timeCardUrl);

        String today = getDate();
        List<WebElement> trList = driver.findElements(By.className("el-table__row"));
        Optional<WebElement> tr = trList.stream().filter(e -> {
            WebElement cell = e.findElement(By.className("cell"));
            if(Objects.nonNull(cell) && today.equals(cell.getText())) {
                return true;
            }
            return false;
        }).findFirst();

        if(tr.isPresent()) {
            List<WebElement> cells = tr.get().findElements(By.className("cell"));
            List<WebElement> inputs = tr.get().findElements(By.className("el-input__inner"));

            String log = java.lang.String.format("%s(%s) 打卡時間:%s~%s", cells.get(0).getText(), cells.get(1).getText()
                    , inputs.get(0).getAttribute("value"), inputs.get(1).getAttribute("value"));

            return log;
        }

        return "無法取得打卡紀錄，請重新檢查";
    }

    /**
     * 取得上班/下班打卡
     * @param args
     * @return
     * @throws ParseException
     */
    private static Pair<TimeCardStatus, Boolean> getCheckinStatus(String[] args) throws ParseException {

        boolean dubug = false;
        Options options = new Options();
        options.addOption("s", "status", true, "上/下班打卡");
        options.addOption("d", "dubug", false, "除錯模式");

        DefaultParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);

        if(!cmdLine.hasOption("s")) {
            System.out.println("尚未輸入參數，ex : -s on/off");
            System.exit(2);
        }

        TimeCardStatus status = TimeCardStatus.findByCode(cmdLine.getOptionValue("s"));

        if(Objects.isNull(status)) {
            System.out.println("輸入參數錯誤，ex : -s on/off");
            System.exit(2);
        }

        if(cmdLine.hasOption("d")) {
            dubug = true;
        }

        return new ImmutablePair<>(status, dubug);
    }


    private static String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    private static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }
}
