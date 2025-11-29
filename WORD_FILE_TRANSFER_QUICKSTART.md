# Word 文档传输功能快速开始

## 📋 概述

该项目已实现 **Word 文档 (.docx) 和任意文件的即时传输功能**。用户可以通过 SIP 协议在两个客户端之间快速发送和接收文件。

## 🚀 快速集成步骤

### 第 1 步：在主控制器中初始化文件传输模块

在 `MainController.java` 的 `initialize()` 方法中添加：

```java
// 初始化文件传输模块
FileTransferIntegration fileTransferIntegration = 
    new FileTransferIntegration(userAgent);

// 设置消息接收回调
fileTransferIntegration.setOnMessageReceived(message -> {
    Platform.runLater(() -> {
        if (message.isFileMessage()) {
            // 创建文件消息界面组件
            FileMessageBox fileBox = new FileMessageBox(message, message.isFromMe());
            messageDisplay.getChildren().add(fileBox);
        } else {
            // 显示文本消息
            displayTextMessage(message);
        }
    });
});

// 设置状态更新回调
fileTransferIntegration.setOnStatusUpdate(status -> {
    Platform.runLater(() -> {
        statusLabel.setText(status);
    });
});
```

### 第 2 步：修改 SIP 消息处理

在 `SipUserAgent.java` 的 `handleIncomingMessage()` 方法中添加文件消息处理：

```java
private void handleIncomingMessage(RequestEvent event) {
    try {
        ServerTransaction transaction = ensureServerTransaction(event);
        Response ok = messageFactory.createResponse(Response.OK, event.getRequest());
        ok.addHeader(contactHeader);
        transaction.sendResponse(ok);
    } catch (Exception ex) {
        System.err.println("Failed to respond to MESSAGE: " + ex.getMessage());
    }
    
    if (messageHandler != null) {
        String fromUri = extractFromUri(event.getRequest());
        byte[] raw = event.getRequest().getRawContent();
        String body = raw == null ? "" : new String(raw, StandardCharsets.UTF_8);
        
        // ✅ 新增：检查是否是文件消息
        if (FileMessage.isFileMessage(body)) {
            // 交由文件传输扩展处理
            if (fileTransferExtension != null) {
                fileTransferExtension.handleIncomingFileMessage(fromUri, body);
            }
        } else {
            // 普通文本消息
            messageHandler.handleIncomingMessage(fromUri, body);
        }
    }
}
```

### 第 3 步：在 GUI 中添加文件选择按钮

在 `main.fxml` 中添加发送文件按钮：

```xml
<Button fx:id="sendFileButton" text="📎 发送文件" 
        onAction="#handleSendFile" 
        style="-fx-font-size: 12; -fx-padding: 8 12;"/>
```

在 `MainController.java` 中实现处理方法：

```java
@FXML
private Button sendFileButton;

@FXML
private void handleSendFile() {
    if (currentContact == null) {
        showAlert("提示", "请先选择联系人");
        return;
    }
    
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("选择要发送的文件");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Word 文档 (*.docx)", "*.docx"),
        new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*")
    );
    
    File selectedFile = fileChooser.showOpenDialog(sendFileButton.getScene().getWindow());
    if (selectedFile != null) {
        try {
            fileTransferIntegration.sendFile(
                currentContact.getSipUri(), 
                selectedFile.getAbsolutePath()
            );
            showAlert("成功", "文件已开始传输: " + selectedFile.getName());
        } catch (Exception e) {
            showAlert("错误", "发送文件失败: " + e.getMessage());
        }
    }
}
```

## 📁 核心文件清单

| 文件 | 功能 | 位置 |
|------|------|------|
| `FileTransferManager.java` | 文件传输核心管理器 | `sip-client/src/main/java/com/example/sipclient/filetransfer/` |
| `FileMessage.java` | 文件消息协议定义 | `sip-client/src/main/java/com/example/sipclient/filetransfer/` |
| `SipFileTransferExtension.java` | SIP 文件传输扩展 | `sip-client/src/main/java/com/example/sipclient/filetransfer/` |
| `FileMessageBox.java` | 文件消息 UI 组件 | `sip-client/src/main/java/com/example/sipclient/gui/component/` |
| `FileTransferIntegration.java` | GUI 与文件传输集成 | `sip-client/src/main/java/com/example/sipclient/gui/integration/` |
| `Message.java` (修改) | 扩展消息模型 | `sip-client/src/main/java/com/example/sipclient/gui/model/` |
| `LocalDatabase.java` (修改) | 数据库支持文件记录 | `sip-client/src/main/java/com/example/sipclient/gui/storage/` |
| `pom.xml` (修改) | 添加 Jackson 依赖 | `sip-client/` |

## 🔧 API 使用示例

### 示例 1：发送 Word 文档

```java
String wordFile = "C:/Documents/report.docx";
String recipient = "sip:user102@10.29.133.174:5060";

fileTransferIntegration.sendFile(recipient, wordFile);
```

### 示例 2：监听文件接收

```java
fileTransferIntegration.setOnMessageReceived(message -> {
    if (message.isFileMessage()) {
        System.out.println("收到文件: " + message.getFileName());
        System.out.println("文件大小: " + Message.formatFileSize(message.getFileSize()));
        System.out.println("存储路径: " + message.getFilePath());
    }
});
```

### 示例 3：获取传输进度

```java
FileTransferManager fileManager = fileTransferIntegration.getFileTransferManager();
fileManager.setListener(new FileTransferManager.FileTransferListener() {
    @Override
    public void onProgress(String fileId, long receivedSize) {
        System.out.printf("已接收: %.2f%%\n", 
            receivedSize * 100.0 / getTotalSize());
    }
    
    // ... 其他方法实现 ...
});
```

## 📊 文件传输流程图

```
┌─────────────────┐
│  发送方选择文件  │
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────────┐
│ 1. 创建文件传输会话                  │
│ 2. 发送 FILE_REQUEST 消息            │
│    (包含: 文件名, 大小, 总块数)      │
└────────┬────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────┐
│ 接收方:                              │
│ - 收到 FILE_REQUEST                  │
│ - 创建接收会话                       │
│ - 发送 FILE_ACK 确认                 │
└────────┬────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────┐
│ 循环传输文件块 (8KB/块):            │
│ 1. 发送 FILE_CHUNK (块数据)          │
│ 2. 接收 FILE_ACK (确认)              │
│ 3. 更新进度                          │
└────────┬────────────────────────────┘
         │
         ↓
┌─────────────────────────────────────┐
│ 传输完成:                            │
│ 1. 所有块已接收                      │
│ 2. 合并块为完整文件                  │
│ 3. 保存到本地目录                    │
│ 4. 发送 FILE_COMPLETE 消息           │
└─────────────────────────────────────┘
```

## 🗄️ 数据库表结构

```sql
-- 扩展的 messages 表
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contact_user_id TEXT NOT NULL,
    content TEXT NOT NULL,
    is_from_me INTEGER NOT NULL,
    timestamp TEXT NOT NULL,
    message_type TEXT DEFAULT 'TEXT',      -- 'TEXT' 或 'FILE'
    file_id TEXT,                          -- 文件传输 ID
    file_name TEXT,                        -- 文件名
    file_size INTEGER,                     -- 文件大小（字节）
    file_path TEXT,                        -- 本地存储路径
    file_status TEXT,                      -- 传输状态
    FOREIGN KEY (contact_user_id) REFERENCES contacts(user_id)
);
```

## 🛡️ 错误处理

所有文件传输操作都包含异常处理：

```java
try {
    fileTransferIntegration.sendFile(targetUri, filePath);
} catch (FileNotFoundException e) {
    showAlert("错误", "文件不存在: " + filePath);
} catch (IOException e) {
    showAlert("错误", "文件读取失败: " + e.getMessage());
} catch (Exception e) {
    showAlert("错误", "传输失败: " + e.getMessage());
}
```

## 🧪 测试场景

### 测试 1：发送单个 Word 文档
```
1. 登录用户 101 和用户 102
2. 用户 101 选择一个 .docx 文件
3. 点击"发送文件"按钮
4. 用户 102 接收并查看进度
5. 验证文件在用户 102 的下载目录中
```

### 测试 2：大文件分块传输
```
1. 发送 50MB+ 的 Word 文档
2. 观察进度条更新
3. 验证最终文件完整性（对比哈希值）
```

### 测试 3：中断后恢复
```
1. 开始发送文件
2. 中途切换到其他应用
3. 继续传输
4. 验证文件完整接收
```

## ⚙️ 配置参数

在 `FileTransferManager.java` 中可调整：

```java
private static final int CHUNK_SIZE = 8192;        // 块大小 (字节)
private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;  // 最大文件 100MB
```

## 📈 性能指标

| 指标 | 值 |
|------|-----|
| 块大小 | 8 KB |
| 平均速度 | 取决于网络 |
| 最大文件 | 100 MB |
| 内存占用 | ~8 KB (每个进行中的块) |

## 🐛 常见问题

**Q: 传输速度太慢？**  
A: 可能是网络问题。检查网络连接，增大 CHUNK_SIZE 可能会改善。

**Q: 文件显示接收中但无进度？**  
A: 检查网络连接。确保 SIP 消息正确传递。

**Q: 接收的文件损坏？**  
A: 检查文件大小是否匹配。启用日志查看详细信息。

**Q: 支持哪些文件格式？**  
A: 支持所有格式（.docx, .pdf, .zip, .exe 等）。

## 🔄 下一步优化

- [ ] 实现断点续传
- [ ] 支持文件压缩
- [ ] 文件预览功能
- [ ] 传输队列管理
- [ ] 加密传输
- [ ] 批量文件发送

---

**最后更新**: 2025年11月29日
