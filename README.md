## 功能

提供自動登入上下班打卡，可搭配其他排程程式來實現自動打卡

```bash
上班打卡 : java -jar TimeCard-1.0-SNAPSHOT.jar -s on
下班打卡 : java -jar TimeCard-1.0-SNAPSHOT.jar -s off
```

## 環境設定
- JDK17、Maven3
- 可使用環境變數(env)或([設定檔](src%2Fmain%2Fresources%2Fconfig.properties))
- [webDriver請自行搭配適合的版本Chrome](https://googlechromelabs.github.io/chrome-for-testing/)
- [OpenAPI申請](https://platform.openai.com/docs/overview)、[Gemini API申請](https://ai.google.dev/gemini-api/docs/api-key?hl=zh-tw)，選擇一個申請即可

```bash
# Web Url
login.url=https://cbs.wits.com/employee/checkIn
time.card.url=https://cbs.wits.com/employee/ossTimeCard

# Chrome/Driver
webdriver.path=D:/chromedriver.exe
chrome.path=C:/Program Files/Google/Chrome/Application/chrome.exe

# OPEN API KEY(需自行申請)
api.key=

# Gemini API KEY(需自行申請)
gemini.api.key=

# 帳號/密碼(登入使用)
user.name=
user.password=
```


## 畫面
![image](https://github.com/jasonSOUI/TimeCard/blob/master/log.png)

## 流程
![image](https://github.com/jasonSOUI/TimeCard/blob/master/uml.png)
