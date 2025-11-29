# 🚀 快速参考卡

## 启动应用

### 方式 1: GUI 应用
```bash
cd sip-client
mvn javafx:run
```

### 方式 2: 用户 101 (命令行)
```bash
cd sip-client
mvn exec:java -Dexec.mainClass="com.example.sipclient.ui.QuickStartUser101"
```

### 方式 3: 用户 102 (命令行)
```bash
cd sip-client
mvn exec:java -Dexec.mainClass="com.example.sipclient.ui.QuickStartUser102"
```

---

## 核心命令

### 编译项目
```bash
mvn clean compile -DskipTests
```

### 运行文件传输测试
```bash
# 所有测试
mvn test

# 特定场景
java -cp target/classes com.example.sipclient.filetransfer.FileTransferTestUtil all
java -cp target/classes com.example.sipclient.filetransfer.FileTransferTestUtil small
java -cp target/classes com.example.sipclient.filetransfer.FileTransferTestUtil large
```

---

## 新增功能

### 📎 发送 Word 文档
```java
fileTransferIntegration.sendFile(
    "sip:user102@10.29.133.174:5060",
    "C:/Documents/report.docx"
);
```

### 📥 接收文件监听
```java
fileTransferIntegration.setOnMessageReceived(message -> {
    if (message.isFileMessage()) {
        System.out.println("收到文件: " + message.getFileName());
    }
});
```

### 📊 获取传输进度
```java
fileTransferIntegration.getFileTransferManager()
    .setListener(new FileTransferManager.FileTransferListener() {
        @Override
        public void onProgress(String fileId, long receivedSize) {
            double percent = receivedSize * 100.0 / totalSize;
            System.out.printf("进度: %.1f%%\n", percent);
        }
        // ... 其他方法
    });
```

---

## 文件路径

### 源代码
```
sip-client/src/main/java/com/example/sipclient/
├── filetransfer/          (文件传输模块)
├── gui/
│   ├── component/         (UI 组件)
│   ├── integration/       (集成模块)
│   └── model/             (修改的 Message.java)
└── ...
```

### 文档
```
项目根目录/
├── WORD_FILE_TRANSFER_GUIDE.md          (详细指南)
├── WORD_FILE_TRANSFER_QUICKSTART.md     (快速开始)
├── PROJECT_RUN_GUIDE.md                 (运行指南)
├── CHANGES_SUMMARY.md                   (变更摘要)
└── PROJECT_COMPLETION.md                (完成总结)
```

---

## 数据文件

### 数据库
```
项目根目录/
└── sip_client.db          (SQLite 数据库)
```

### 下载目录
```
用户主目录/SipClientFiles/
└── (接收的文件)
```

---

## 关键类

| 类名 | 位置 | 功能 |
|------|------|------|
| FileTransferManager | filetransfer/ | 核心管理器 |
| FileMessage | filetransfer/ | 消息协议 |
| SipFileTransferExtension | filetransfer/ | SIP 扩展 |
| FileMessageBox | gui/component/ | UI 组件 |
| FileTransferIntegration | gui/integration/ | GUI 集成 |
| Message | gui/model/ | 消息模型 (修改) |
| LocalDatabase | gui/storage/ | 数据库 (修改) |

---

## 配置参数

### 文件大小限制
```java
FileTransferManager.MAX_FILE_SIZE = 100 * 1024 * 1024;  // 100 MB
```

### 块大小
```java
FileTransferManager.CHUNK_SIZE = 8192;  // 8 KB
```

### 下载目录
```java
String downloadDir = System.getProperty("user.home") + "/SipClientFiles";
```

---

## 消息类型

```java
// 文件传输消息类型
FileMessage.TYPE_FILE_REQUEST      // 文件请求
FileMessage.TYPE_FILE_CHUNK        // 块数据
FileMessage.TYPE_FILE_ACK          // 确认
FileMessage.TYPE_FILE_COMPLETE     // 完成
FileMessage.TYPE_FILE_CANCEL       // 取消
```

---

## 状态值

```java
// 文件状态
"SENDING"      // 发送中
"RECEIVING"    // 接收中
"COMPLETED"    // 已完成
"FAILED"       // 失败
"CANCELLED"    // 已取消
```

---

## 日志查看

### 启用调试日志
编辑 `src/main/resources/logback.xml`:
```xml
<root level="DEBUG">
    <appender-ref ref="FILE"/>
    <appender-ref ref="CONSOLE"/>
</root>
```

### 日志位置
```
用户主目录/.SipClient/app.log
```

---

## 常见错误排查

### ❌ 编译失败
```
✓ 检查 JDK 版本 (需要 17+)
✓ 清除 Maven 缓存: mvn clean install
✓ 检查网络连接
```

### ❌ 应用无法启动
```
✓ 确保有 SIP 服务器运行
✓ 检查端口 5060 是否被占用
✓ 查看日志文件
```

### ❌ 文件传输失败
```
✓ 检查网络连接
✓ 验证 SIP 消息能正确传递
✓ 检查磁盘空间
✓ 查看日志文件诊断
```

---

## 测试场景

### 场景 1: 发送小文件
```bash
启动用户 101 和 102
在 101 中: msg sip:102@host:5060 hello
在 102 中: 收到消息
```

### 场景 2: 发送 Word 文档
```bash
GUI 中点击 "📎 发送文件"
选择 .docx 文件
观察进度条
对方收到文件
```

### 场景 3: 发送大文件
```bash
运行 FileTransferTestUtil
java -cp target/classes com.example.sipclient.filetransfer.FileTransferTestUtil large
验证完整性
```

---

## 性能优化

### 提高速度
```java
// 增大块大小（需权衡）
CHUNK_SIZE = 16384;  // 16 KB
```

### 减少内存占用
```java
// 实现流式处理
// 避免一次加载整个文件
```

### 并发传输
```java
// 系统已支持多文件同时传输
// 无需额外配置
```

---

## 依赖版本

```
Java                17+
JavaFX              21.0.1
JAIN SIP            1.3.0-91
Spring Boot         3.2.5
SQLite JDBC         3.45.0.0
Jackson             2.16.0
Maven               3.6+
```

---

## 项目统计

```
✅ 代码行数:    1,500+
✅ 新增文件:    6 个
✅ 修改文件:    3 个
✅ 文档行数:    3,000+
✅ 编译耗时:    ~5 秒
✅ 启动耗时:    ~3 秒
```

---

## 联系信息

- 📖 完整指南: 见 WORD_FILE_TRANSFER_GUIDE.md
- 🚀 快速开始: 见 WORD_FILE_TRANSFER_QUICKSTART.md
- 🏃 运行指南: 见 PROJECT_RUN_GUIDE.md
- ✅ 完成总结: 见 PROJECT_COMPLETION.md

---

## 下一步

1. 📖 阅读相应文档了解详情
2. 🏃 运行快速开始示例
3. 🧪 使用测试工具验证功能
4. 💡 根据需要定制和扩展

---

**祝你使用愉快！🎉**

*最后更新: 2025年11月29日*
