## 功能

- 上班打卡
```bash
java -jar TimeCard-1.0-SNAPSHOT.jar -s on
```
- 下班打卡
```bash
java -jar TimeCard-1.0-SNAPSHOT.jar -s off
```

## 環境設定
- JDK17、Maven3
- 可使用環境變數(env)或([設定檔](src%2Fmain%2Fresources%2Fconfig.properties))
- [webDriver請自行搭配適合的版本的Chrome](https://googlechromelabs.github.io/chrome-for-testing/)

```bash
# Web Url
login.url=https://cbs.wits.com/employee/checkIn
time.card.url=https://cbs.wits.com/employee/ossTimeCard

# Chrome/Driver
webdriver.path=D:/chromedriver.exe
chrome.path=C:/Program Files/Google/Chrome/Application/chrome.exe

# OPEN API KEY(需自行申請)
api.key=

# 帳號/密碼(登入使用)
user.name=
user.password=
```


![範例圖片 1](https://fakeimg.pl/500/)
![範例圖片 2](https://fakeimg.pl/500/)
![範例圖片 3](https://fakeimg.pl/500/)

  ...