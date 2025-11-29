# Word 文档传输功能使用指南

## 功能概述

该项目现已支持 **Word 文档 (.docx) 和其他文件的即时传输**，通过 SIP 协议进行点对点文件分块传输，具有以下特点：

- ✅ 支持任何类型文件（特别是 Word 文档）
- ✅ 自动分块上传和下载（8KB 每块）
- ✅ 实时进度显示
- ✅ 本地数据库存储文件传输记录
- ✅ 文件接收完成后可直接打开
- ✅ 最大文件大小支持 100MB

## 架构设计

### 1. 核心组件

#### FileTransferManager (文件传输管理器)
```
位置: sip-client/src/main/java/com/example/sipclient/filetransfer/FileTransferManager.java

功能：
- 管理文件发送和接收会话
- 实现 8KB 分块传输
- 跟踪传输进度
- 自动合并接收块为完整文件
```

#### FileMessage (文件消息协议)
```
位置: sip-client/src/main/java/com/example/sipclient/filetransfer/FileMessage.java

消息类型：
- FILE_REQUEST    - 文件传输开始请求
- FILE_CHUNK      - 文件块数据
- FILE_ACK        - 块确认
- FILE_COMPLETE   - 传输完成
- FILE_CANCEL     - 传输取消
```

#### SipFileTransferExtension (SIP 文件传输扩展)
```
位置: sip-client/src/main/java/com/example/sipclient/filetransfer/SipFileTransferExtension.java

功能：
- 在 SIP MESSAGE 上扩展文件传输协议
- 处理文件请求和块传输
- 自动确认机制
```

### 2. 数据存储

#### 数据库扩展 (LocalDatabase)
```sql
-- messages 表新增字段
ALTER TABLE messages ADD COLUMN message_type TEXT DEFAULT 'TEXT';
ALTER TABLE messages ADD COLUMN file_id TEXT;
ALTER TABLE messages ADD COLUMN file_name TEXT;
ALTER TABLE messages ADD COLUMN file_size INTEGER;
ALTER TABLE messages ADD COLUMN file_path TEXT;
ALTER TABLE messages ADD COLUMN file_status TEXT;
```

### 3. 消息模型扩展 (Message)

```java
// 支持两种消息类型
public enum MessageType {
    TEXT,      // 文本消息
    FILE       // 文件消息
}

// 文件消息额外字段
private String fileId;        // 文件传输 ID
private String fileName;      // 文件名
private long fileSize;        // 文件大小
private String filePath;      // 本地路径
private String fileStatus;    // 传输状态
```

## 使用方法

### 1. 初始化文件传输管理器

```java
// 在主应用启动时
String downloadDir = System.getProperty("user.home") + "/SipClientFiles";
FileTransferManager fileTransferManager = new FileTransferManager(downloadDir);

SipFileTransferExtension fileTransferExtension = 
    new SipFileTransferExtension(userAgent, fileTransferManager);

// 设置监听器
fileTransferManager.setListener(new FileTransferManager.FileTransferListener() {
    @Override
    public void onTransferStarted(String fileId, String fileName, long totalSize) {
        System.out.println("开始传输: " + fileName);
    }
    
    @Override
    public void onProgress(String fileId, long receivedSize) {
        // 更新 GUI 进度条
    }
    
    @Override
    public void onTransferCompleted(String fileId, String filePath) {
        System.out.println("传输完成: " + filePath);
    }
    
    @Override
    public void onTransferFailed(String fileId, String errorMessage) {
        System.err.println("传输失败: " + errorMessage);
    }
    
    @Override
    public void onTransferCancelled(String fileId) {
        System.out.println("传输已取消");
    }
});
```

### 2. 发送 Word 文件

```java
// 发送文件
String wordFilePath = "C:/Documents/report.docx";
String targetUri = "sip:user102@10.29.133.174:5060";

try {
    fileTransferExtension.sendFile(targetUri, wordFilePath);
} catch (Exception e) {
    System.err.println("发送文件失败: " + e.getMessage());
}
```

### 3. 接收文件处理

在 SipUserAgent 中修改 `handleIncomingMessage` 方法：

```java
private void handleIncomingMessage(RequestEvent event) {
    // ... 现有代码 ...
    
    if (messageHandler != null) {
        String fromUri = extractFromUri(event.getRequest());
        byte[] raw = event.getRequest().getRawContent();
        String body = raw == null ? "" : new String(raw, StandardCharsets.UTF_8);
        
        // 检查是否是文件消息
        if (FileMessage.isFileMessage(body)) {
            // 交由文件传输扩展处理
            fileTransferExtension.handleIncomingFileMessage(fromUri, body);
        } else {
            // 普通文本消息
            messageHandler.handleIncomingMessage(fromUri, body);
        }
    }
}
```

### 4. GUI 集成

在 MainController 中显示文件消息：

```java
private void displayMessage(Message msg) {
    if (msg.isFileMessage()) {
        // 使用 FileMessageBox 组件显示文件
        FileMessageBox fileBox = new FileMessageBox(msg, msg.isFromMe());
        messageDisplay.getChildren().add(fileBox);
        
        // 设置文件传输监听
        fileTransferManager.setListener(new FileTransferManager.FileTransferListener() {
            @Override
            public void onProgress(String fileId, long receivedSize) {
                if (fileId.equals(msg.getFileId())) {
                    fileBox.updateProgress(receivedSize);
                }
            }
            
            @Override
            public void onTransferCompleted(String fileId, String filePath) {
                if (fileId.equals(msg.getFileId())) {
                    fileBox.setTransferCompleted(filePath);
                }
            }
            
            @Override
            public void onTransferFailed(String fileId, String errorMessage) {
                if (fileId.equals(msg.getFileId())) {
                    fileBox.setTransferFailed(errorMessage);
                }
            }
            
            @Override
            public void onTransferCancelled(String fileId) {}
        });
    } else {
        // 显示文本消息
        displayTextMessage(msg);
    }
}
```

## 传输流程

### 发送流程

```
1. 用户选择 Word 文件
   ↓
2. FileTransferManager 创建传输会话
   ↓
3. SipFileTransferExtension 发送 FILE_REQUEST 消息
   ↓
4. 逐块读取文件数据 (8KB/块)
   ↓
5. 发送 FILE_CHUNK 消息
   ↓
6. 等待接收方 FILE_ACK 确认
   ↓
7. 所有块传输完成
   ↓
8. 发送 FILE_COMPLETE 消息
```

### 接收流程

```
1. 收到 FILE_REQUEST 消息
   ↓
2. FileTransferManager 创建接收会话
   ↓
3. GUI 显示文件接收提示
   ↓
4. 收到 FILE_CHUNK 消息
   ↓
5. 保存块数据并发送 FILE_ACK 确认
   ↓
6. 更新进度条
   ↓
7. 收到 FILE_COMPLETE 消息
   ↓
8. 合并所有块为完整文件
   ↓
9. 文件保存到本地目录，显示"已完成"状态
```

## 文件路径和配置

### 默认下载目录

```
Windows: C:\Users\{username}\Documents\SipClientFiles\
Linux:   /home/{username}/SipClientFiles/
macOS:   /Users/{username}/SipClientFiles/
```

### 数据库存储

接收的文件记录存储在 SQLite 本地数据库中：

```sql
SELECT * FROM messages 
WHERE message_type = 'FILE' 
AND contact_user_id = 'user102';
```

## 文件限制

| 项目 | 限制 |
|------|------|
| 最大文件大小 | 100 MB |
| 单块大小 | 8 KB |
| 支持文件类型 | 所有（包括 .docx） |
| 同时传输数 | 无限制 |

## 错误处理

### 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| 文件不存在 | 文件路径错误 | 检查文件路径 |
| 文件过大 | >100MB | 分割文件后传输 |
| 传输中断 | 网络问题 | 点击重试 |
| 权限拒绝 | 文件权限不足 | 检查文件权限 |

## 代码示例：完整的文件传输场景

```java
public class WordFileTransferExample {
    
    public static void main(String[] args) throws Exception {
        // 1. 初始化 SIP 客户端
        SipConfig config = new SipConfig(
            "sip:user101@10.29.133.174:5060",
            "password",
            "10.29.133.174",
            5060
        );
        
        SipUserAgent userAgent = new SipUserAgent();
        userAgent.initialize(config);
        userAgent.register();
        
        // 2. 初始化文件传输
        FileTransferManager fileTransferManager = 
            new FileTransferManager(System.getProperty("user.home") + "/SipClientFiles");
        
        SipFileTransferExtension fileTransfer = 
            new SipFileTransferExtension(userAgent, fileTransferManager);
        
        // 3. 设置监听器
        fileTransferManager.setListener(new FileTransferManager.FileTransferListener() {
            @Override
            public void onTransferStarted(String fileId, String fileName, long totalSize) {
                System.out.printf("📤 发送文件: %s (%s)%n", 
                    fileName, formatSize(totalSize));
            }
            
            @Override
            public void onProgress(String fileId, long receivedSize) {
                // 进度更新
            }
            
            @Override
            public void onTransferCompleted(String fileId, String filePath) {
                System.out.printf("✓ 传输完成: %s%n", filePath);
            }
            
            @Override
            public void onTransferFailed(String fileId, String errorMessage) {
                System.err.printf("✗ 传输失败: %s%n", errorMessage);
            }
            
            @Override
            public void onTransferCancelled(String fileId) {
                System.out.println("取消传输");
            }
        });
        
        // 4. 发送 Word 文档
        String wordFile = "C:/Documents/project_report.docx";
        String recipient = "sip:user102@10.29.133.174:5060";
        
        fileTransfer.sendFile(recipient, wordFile);
        
        // 保持连接
        Thread.sleep(60000);
        
        userAgent.unregister();
    }
    
    private static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}
```

## 性能优化建议

1. **分块大小调整**：可在 FileTransferManager 中修改 CHUNK_SIZE
2. **多线程传输**：实现并发块发送以提高速度
3. **压缩传输**：对大文件启用 gzip 压缩
4. **断点续传**：保存传输进度支持中断后恢复

## 未来扩展

- [ ] 文件预览功能
- [ ] 传输速度限制
- [ ] 批量文件传输
- [ ] 文件加密传输
- [ ] 云存储集成
- [ ] 群文件共享

---

**集成时间**: 2025年11月29日  
**支持版本**: 1.0.0+
