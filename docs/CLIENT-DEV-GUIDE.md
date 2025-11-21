# Java PC 客户端开发指南

> 使用 JavaFX 开发桌面 SIP 通信客户端

## 目录

- [技术选型](#技术选型)
- [项目结构](#项目结构)
- [界面设计](#界面设计)
- [核心功能](#核心功能)
- [开发步骤](#开发步骤)

---

## 技术选型

### JavaFX vs Swing

| 特性 | JavaFX | Swing |
|------|--------|-------|
| 外观 | 现代、美观 | 传统、朴素 |
| 性能 | 良好 | 优秀 |
| 学习曲线 | 中等 | 简单 |
| CSS 支持 | ✅ | ❌ |
| 图表组件 | ✅ 内置 | ❌ 需第三方库 |
| 社区支持 | 活跃 | 逐渐减少 |
| 推荐指数 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

**推荐使用 JavaFX**

---

## 项目结构

```
sip-client/src/main/java/com/example/sipclient/
├── ui/
│   ├── ConsoleMain.java          # 命令行界面（已有）
│   └── gui/                      # GUI 界面（新建）
│       ├── MainApplication.java  # JavaFX 主应用
│       ├── controller/           # FXML 控制器
│       │   ├── LoginController.java
│       │   ├── MainWindowController.java
│       │   ├── ChatController.java
│       │   ├── ContactsController.java
│       │   └── CallController.java
│       ├── model/                # 视图模型
│       │   ├── UserViewModel.java
│       │   └── MessageViewModel.java
│       └── service/              # UI 服务层
│           ├── ApiService.java   # 与服务器通信
│           └── SipClientService.java # SIP 客户端封装
├── api/                          # API 客户端（新建）
│   ├── ServerApiClient.java     # REST API 客户端
│   └── dto/                      # 数据传输对象
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       └── MessageDto.java
└── resources/                    # 资源文件（新建）
    ├── fxml/                     # FXML 界面文件
    │   ├── login.fxml
    │   ├── main-window.fxml
    │   ├── chat.fxml
    │   └── contacts.fxml
    ├── css/                      # 样式文件
    │   └── style.css
    └── images/                   # 图片资源
        ├── logo.png
        └── icons/
```

---

## 界面设计

### 登录窗口

```
┌─────────────────────────────────────┐
│         SIP 通信客户端                 │
│                                     │
│   ┌─────────────────────────────┐  │
│   │  SIP URI:                    │  │
│   │  ┌─────────────────────────┐│  │
│   │  │ sip:alice@server        ││  │
│   │  └─────────────────────────┘│  │
│   │                              │  │
│   │  密码:                        │  │
│   │  ┌─────────────────────────┐│  │
│   │  │ ********                ││  │
│   │  └─────────────────────────┘│  │
│   │                              │  │
│   │  本地 IP:                     │  │
│   │  ┌─────────────────────────┐│  │
│   │  │ 192.168.1.50            ││  │
│   │  └─────────────────────────┘│  │
│   │                              │  │
│   │  本地端口:                    │  │
│   │  ┌─────────────────────────┐│  │
│   │  │ 5070                    ││  │
│   │  └─────────────────────────┘│  │
│   │                              │  │
│   │     [登录]     [设置]         │  │
│   └─────────────────────────────┘  │
└─────────────────────────────────────┘
```

### 主窗口

```
┌───────────────────────────────────────────────────────────┐
│  SIP 通信客户端                   alice@server   [最小化] [×]│
├────────────┬──────────────────────────────────────────────┤
│            │  消息                                          │
│  联系人列表   │  ┌────────────────────────────────────────┐  │
│            │  │ 👤 Alice                                │  │
│ 🔍 搜索...  │  │ ───────────────────────────────────── │  │
│            │  │ Alice: 你好！                          │  │
│ 👤 Bob     │  │ 10:30                                  │  │
│   最近消息... │  │                                        │  │
│   2 未读    │  │ Me: 你好，在吗？                        │  │
│            │  │ 10:31                                  │  │
│ 👤 Charlie │  │                                        │  │
│   在线      │  │ Alice: 在的，有什么事吗？                │  │
│            │  │ 10:32                                  │  │
│ 👤 David   │  │                                        │  │
│   离线      │  │                                        │  │
│            │  └────────────────────────────────────────┘  │
│            │                                              │
│  [+ 添加]   │  ┌────────────────────────────────────────┐  │
│            │  │ 输入消息...                    [📎] [😊]│  │
│            │  └────────────────────────────────────────┘  │
│            │  [发送]                                      │
├────────────┼──────────────────────────────────────────────┤
│ alice@se...│  [💬 消息] [📞 通话] [👥 联系人] [⚙️ 设置]     │
└────────────┴──────────────────────────────────────────────┘
```

### 通话窗口

```
┌─────────────────────────────────────┐
│  正在通话 - Bob                       │
│                                     │
│           👤                        │
│           Bob                       │
│                                     │
│         00:05:23                    │
│                                     │
│   [🔇 静音]  [⏸️ 保持]  [📞 挂断]     │
│                                     │
└─────────────────────────────────────┘
```

---

## 核心功能

### 1. 用户认证

**LoginController.java**
```java
@FXML
private void handleLogin() {
    String sipUri = sipUriField.getText();
    String password = passwordField.getText();
    String localIp = localIpField.getText();
    int localPort = Integer.parseInt(localPortField.getText());
    
    // 调用 API 登录
    apiService.login(sipUri, password, localIp, localPort)
        .thenAccept(response -> {
            // 保存 Token
            Session.setToken(response.getToken());
            Session.setUserId(response.getUserId());
            
            // 初始化 SIP 客户端
            sipClientService.init(sipUri, password, localIp, localPort);
            
            // 跳转到主窗口
            Platform.runLater(() -> showMainWindow());
        })
        .exceptionally(ex -> {
            Platform.runLater(() -> showError("登录失败: " + ex.getMessage()));
            return null;
        });
}
```

### 2. 联系人列表

**ContactsController.java**
```java
@FXML
private ListView<User> contactsListView;

public void initialize() {
    // 加载联系人
    apiService.getUsers()
        .thenAccept(users -> {
            Platform.runLater(() -> {
                contactsListView.getItems().setAll(users);
            });
        });
    
    // 监听选择
    contactsListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openChatWith(newVal);
            }
        });
}
```

### 3. 消息收发

**ChatController.java**
```java
@FXML
private TextArea messageInput;
@FXML
private ListView<Message> messageListView;

@FXML
private void sendMessage() {
    String content = messageInput.getText();
    if (content.isEmpty()) return;
    
    User currentContact = getCurrentContact();
    
    // 通过 SIP 发送消息
    sipClientService.sendMessage(currentContact.getSipUri(), content)
        .thenAccept(success -> {
            // 添加到界面
            Platform.runLater(() -> {
                Message msg = new Message(
                    Session.getUserId(),
                    currentContact.getSipUri(),
                    content,
                    System.currentTimeMillis()
                );
                messageListView.getItems().add(msg);
                messageInput.clear();
            });
            
            // 同步到服务器
            apiService.saveMessage(currentContact.getSipUri(), content);
        });
}

// 监听收到的消息
private void setupMessageListener() {
    sipClientService.setMessageListener((from, content) -> {
        Platform.runLater(() -> {
            Message msg = new Message(from, Session.getUserId(), content, System.currentTimeMillis());
            messageListView.getItems().add(msg);
            
            // 播放提示音
            playNotificationSound();
        });
    });
}
```

### 4. 语音通话

**CallController.java**
```java
@FXML
private Label statusLabel;
@FXML
private Label durationLabel;

public void initiateCall(String targetSipUri) {
    sipClientService.startCall(targetSipUri)
        .thenAccept(callId -> {
            this.currentCallId = callId;
            Platform.runLater(() -> {
                statusLabel.setText("呼叫中...");
                startCallTimer();
            });
        });
}

@FXML
private void hangup() {
    sipClientService.hangupCall(currentCallId)
        .thenRun(() -> {
            Platform.runLater(() -> {
                stopCallTimer();
                close();
            });
        });
}

// 监听来电
private void setupCallListener() {
    sipClientService.setIncomingCallListener((from, callId) -> {
        Platform.runLater(() -> {
            showIncomingCallDialog(from, callId);
        });
    });
}
```

---

## 开发步骤

### 步骤 1：添加 JavaFX 依赖

编辑 `sip-client/pom.xml`：

```xml
<dependencies>
    <!-- 现有依赖 -->
    
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.2</version>
    </dependency>
    
    <!-- HTTP 客户端 -->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.11.0</version>
    </dependency>
    
    <!-- JSON 处理 -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

### 步骤 2：创建 MainApplication

创建 `src/main/java/com/example/sipclient/ui/gui/MainApplication.java`：

```java
package com.example.sipclient.ui.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/login.fxml")
        );
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        
        primaryStage.setTitle("SIP 通信客户端");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

### 步骤 3：创建 FXML 布局

创建 `src/main/resources/fxml/login.fxml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<VBox xmlns="http://javafx.com/javafx"
      xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.example.sipclient.ui.gui.controller.LoginController"
      spacing="15" alignment="CENTER" styleClass="login-container">
    
    <padding>
        <Insets top="40" right="40" bottom="40" left="40"/>
    </padding>
    
    <Label text="SIP 通信客户端" styleClass="title"/>
    
    <VBox spacing="10">
        <Label text="SIP URI:"/>
        <TextField fx:id="sipUriField" promptText="sip:alice@192.168.1.100:5060"/>
        
        <Label text="密码:"/>
        <PasswordField fx:id="passwordField" promptText="请输入密码"/>
        
        <Label text="本地 IP:"/>
        <TextField fx:id="localIpField" promptText="192.168.1.50"/>
        
        <Label text="本地端口:"/>
        <TextField fx:id="localPortField" text="5070"/>
    </VBox>
    
    <HBox spacing="10" alignment="CENTER">
        <Button text="登录" onAction="#handleLogin" styleClass="primary-button"/>
        <Button text="设置" onAction="#handleSettings"/>
    </HBox>
    
    <Label fx:id="statusLabel" styleClass="status-label"/>
    
</VBox>
```

### 步骤 4：创建控制器

创建 `src/main/java/com/example/sipclient/ui/gui/controller/LoginController.java`：

```java
package com.example.sipclient.ui.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

public class LoginController {
    
    @FXML private TextField sipUriField;
    @FXML private PasswordField passwordField;
    @FXML private TextField localIpField;
    @FXML private TextField localPortField;
    @FXML private Label statusLabel;
    
    @FXML
    private void handleLogin() {
        String sipUri = sipUriField.getText();
        String password = passwordField.getText();
        String localIp = localIpField.getText();
        String localPort = localPortField.getText();
        
        // 验证输入
        if (sipUri.isEmpty() || password.isEmpty()) {
            statusLabel.setText("请填写所有必填字段");
            return;
        }
        
        statusLabel.setText("正在登录...");
        
        // TODO: 调用 API 登录
        // TODO: 初始化 SIP 客户端
        // TODO: 跳转到主窗口
    }
    
    @FXML
    private void handleSettings() {
        // TODO: 打开设置窗口
    }
}
```

### 步骤 5：添加 CSS 样式

创建 `src/main/resources/css/style.css`：

```css
.login-container {
    -fx-background-color: #f5f5f5;
}

.title {
    -fx-font-size: 24px;
    -fx-font-weight: bold;
    -fx-text-fill: #333;
}

.primary-button {
    -fx-background-color: #007bff;
    -fx-text-fill: white;
    -fx-padding: 10px 20px;
    -fx-font-size: 14px;
    -fx-cursor: hand;
}

.primary-button:hover {
    -fx-background-color: #0056b3;
}

.status-label {
    -fx-text-fill: #666;
    -fx-font-size: 12px;
}
```

### 步骤 6：创建 API 客户端

创建 `src/main/java/com/example/sipclient/api/ServerApiClient.java`：

```java
package com.example.sipclient.api;

import okhttp3.*;
import com.google.gson.Gson;
import java.util.concurrent.CompletableFuture;

public class ServerApiClient {
    
    private static final String BASE_URL = "http://localhost:8081/api";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private String token;
    
    public CompletableFuture<LoginResponse> login(LoginRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = gson.toJson(request);
                RequestBody body = RequestBody.create(
                    json, MediaType.parse("application/json")
                );
                
                Request httpRequest = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .post(body)
                    .build();
                
                Response response = client.newCall(httpRequest).execute();
                String responseBody = response.body().string();
                
                ApiResponse<LoginResponse> apiResponse = gson.fromJson(
                    responseBody, 
                    new TypeToken<ApiResponse<LoginResponse>>(){}.getType()
                );
                
                if (apiResponse.isSuccess()) {
                    this.token = apiResponse.getData().getToken();
                    return apiResponse.getData();
                } else {
                    throw new RuntimeException(apiResponse.getError());
                }
            } catch (Exception e) {
                throw new RuntimeException("登录失败: " + e.getMessage(), e);
            }
        });
    }
    
    // 其他 API 方法...
}
```

### 步骤 7：运行应用

```powershell
cd sip-client
mvn javafx:run
```

或在 IDE 中运行 `MainApplication.main()`

---

## 高级功能

### 1. 消息通知

```java
public class NotificationService {
    
    public void showNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                TrayIcon trayIcon = new TrayIcon(
                    Toolkit.getDefaultToolkit().getImage("icon.png"),
                    "SIP 客户端"
                );
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

### 2. 表情包支持

```java
@FXML
private void showEmojiPicker() {
    EmojiPicker picker = new EmojiPicker();
    picker.setOnEmojiSelected(emoji -> {
        messageInput.appendText(emoji);
    });
    picker.show();
}
```

### 3. 文件传输

```java
@FXML
private void sendFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(stage);
    
    if (file != null) {
        fileTransferService.sendFile(file, currentContact)
            .thenAccept(success -> {
                // 显示发送成功
            });
    }
}
```

---

## 调试技巧

### 1. 使用 Scene Builder

Scene Builder 是 JavaFX 的可视化 FXML 编辑器：
- 下载：https://gluonhq.com/products/scene-builder/
- 导入 FXML 文件
- 拖拽组件设计界面
- 导出 FXML

### 2. 日志调试

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML
    private void handleLogin() {
        logger.info("用户尝试登录: {}", sipUriField.getText());
        // ...
    }
}
```

### 3. 异常处理

```java
@FXML
private void handleLogin() {
    try {
        // 登录逻辑
    } catch (Exception e) {
        logger.error("登录失败", e);
        showErrorDialog("登录失败", e.getMessage());
    }
}

private void showErrorDialog(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.showAndWait();
}
```

---

## 参考资源

- **JavaFX 官方文档**: https://openjfx.io/
- **JavaFX 教程**: https://docs.oracle.com/javafx/2/get_started/jfxpub-get_started.htm
- **FXML 指南**: https://docs.oracle.com/javafx/2/api/javafx/fxml/doc-files/introduction_to_fxml.html
- **CSS 参考**: https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html

---

**开始构建你的 GUI 客户端吧！** 🎨
