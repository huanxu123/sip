# 项目运行指南 - Word 文件传输功能

## ✅ 编译状态：成功 ✅

项目已成功编译！所有 29 个 SIP 客户端源文件和 25 个管理服务器源文件都已编译成功。

```
BUILD SUCCESS
Total time: 5.067 s
```

## 🚀 应用启动方式

### 方式 1：GUI 应用（推荐）

#### Windows PowerShell 启动：
```powershell
.\start-gui.ps1
```

#### 或使用 Maven 启动：
```bash
cd sip-client
mvn javafx:run
```

#### 或直接运行：
```bash
mvn -f sip-client/pom.xml javafx:run
```

### 方式 2：命令行客户端

启动用户 101：
```powershell
.\start-sip-user101.ps1
```

启动用户 102：
```powershell
.\start-sip-user102.ps1
```

### 方式 3：业务服务器

```bash
cd admin-server
mvn spring-boot:run
```

服务器将运行在 `http://localhost:8081`

## 📋 应用功能菜单

### GUI 应用功能

启动后，你将看到以下功能：

#### 1. 登录界面
- 输入 SIP URI（例如：`sip:user101@10.29.133.174:5060`）
- 输入密码
- 指定本地 IP 和端口
- 点击"登录"

#### 2. 主聊天界面
- **联系人列表** - 显示所有联系人
- **聊天窗口** - 显示消息记录
- **输入框** - 发送文本消息
- **发送按钮** - 发送消息或文件

#### 3. 📎 Word 文件传输（新功能）
- 点击 "📎 发送文件" 按钮
- 选择 Word 文档 (.docx) 或任意文件
- 文件会自动分块发送
- 进度条显示传输进度
- 接收方自动接收和保存文件

#### 4. 设置界面
- 通知设置
- 主题切换（浅色/深色）
- 音量调节
- 聊天记录保存设置

## 🔧 系统要求

| 组件 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| JavaFX | 21.0.1 |
| SIP 协议库 | JAIN SIP 1.3.0 |
| SQLite JDBC | 3.45.0.0 |
| Jackson (JSON) | 2.16.0 |

## 📂 项目结构

```
Network-Communication-Software-Major-Project-main/
├── sip-client/                    # Java 桌面客户端（JavaFX）
│   ├── src/main/java/
│   │   └── com/example/sipclient/
│   │       ├── gui/               # GUI 界面
│   │       ├── sip/               # SIP 协议实现
│   │       ├── chat/              # 聊天功能
│   │       ├── call/              # 呼叫管理
│   │       ├── filetransfer/      # ✨ 文件传输（新增）
│   │       └── ui/                # 命令行界面
│   └── pom.xml                    # Maven 配置
│
├── admin-server/                  # Spring Boot 业务服务器
│   ├── src/main/java/
│   │   └── com/example/admin/
│   │       ├── controller/        # REST API
│   │       ├── service/           # 业务逻辑
│   │       └── util/              # 工具类
│   └── pom.xml                    # Maven 配置
│
├── pom.xml                        # 父 POM
├── start-gui.ps1                  # GUI 启动脚本
├── start-sip-user101.ps1          # 用户 101 启动脚本
├── start-sip-user102.ps1          # 用户 102 启动脚本
│
├── WORD_FILE_TRANSFER_GUIDE.md    # 文件传输详细指南
├── WORD_FILE_TRANSFER_QUICKSTART.md  # 快速开始
└── README.md                      # 项目说明
```

## 📊 新增文件传输功能

### 核心模块

1. **FileTransferManager.java**
   - 管理文件收发会话
   - 实现 8KB 分块传输
   - 跟踪传输进度
   - 自动合并接收块

2. **FileMessage.java**
   - 文件消息协议定义
   - 支持 5 种消息类型：REQUEST, CHUNK, ACK, COMPLETE, CANCEL
   - JSON 编码/解码

3. **SipFileTransferExtension.java**
   - SIP 消息扩展处理
   - 文件传输流程编排

4. **FileMessageBox.java**
   - GUI 文件消息显示组件
   - 进度条、打开按钮等

5. **FileTransferIntegration.java**
   - GUI 与文件传输的桥接类
   - 消息回调和事件处理

### 数据库扩展

messages 表新增字段：
```sql
message_type      -- 'TEXT' 或 'FILE'
file_id           -- 文件传输 ID
file_name         -- 文件名
file_size         -- 文件大小
file_path         -- 本地路径
file_status       -- 传输状态
```

## 🧪 测试场景

### 测试 1：发送小文件
```
1. 启动两个客户端（用户 101 和 102）
2. 用户 101 点击"发送文件"
3. 选择一个 .docx 文件
4. 观察进度条
5. 用户 102 收到文件提示
6. 点击"打开"查看文件
```

### 测试 2：发送大文件
```
1. 选择 50MB+ 的 Word 文档
2. 观察分块传输过程
3. 监控网络带宽使用
4. 验证文件完整性
```

### 测试 3：并发传输
```
1. 用户 101 向用户 102 发送文件 A
2. 同时用户 102 向用户 101 发送文件 B
3. 两个传输应并行进行
4. 都能成功完成
```

## 📈 性能指标

| 指标 | 值 |
|------|-----|
| 单块大小 | 8 KB |
| 最大文件 | 100 MB |
| 支持格式 | 所有（包括 .docx） |
| 平均速度 | 取决于网络 |
| 内存占用 | ~8 KB/块 |

## 🔐 文件安全

- ✅ 自动分块验证
- ✅ 文件完整性检查（可选 MD5）
- ✅ 本地加密存储（SQLite）
- ✅ 自动创建接收目录

## 💾 文件存储位置

### 默认下载目录
```
Windows: %USERPROFILE%\SipClientFiles\
Linux:   ~/SipClientFiles/
macOS:   ~/SipClientFiles/
```

### 数据库位置
```
当前目录: sip_client.db (SQLite)
```

## 🚨 常见问题

### Q: 应用启动后无法看到窗口？
A: 检查防火墙设置，或查看控制台输出获取错误信息。

### Q: 编译失败提示缺少依赖？
A: 运行 `mvn clean dependency:resolve`，然后重新编译。

### Q: 文件传输速度慢？
A: 这可能是网络问题。检查网络连接和 SIP 服务器状态。

### Q: 接收的文件损坏？
A: 检查文件大小是否匹配原文件。查看日志了解传输过程。

### Q: 如何更改下载目录？
A: 修改 `FileTransferIntegration` 中的 `downloadDir` 参数。

## 📝 日志位置

应用日志由 Logback 管理：

```
sip-client/src/main/resources/logback.xml
```

## 🔄 依赖更新

项目已添加 Jackson JSON 处理库：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>
```

## 📞 SIP 连接配置

### 默认 MSS 服务器
```
主机: 10.29.133.174
端口: 5060
```

### 用户账号
```
用户 101: sip:101@10.29.133.174:5060
用户 102: sip:102@10.29.133.174:5060
```

## 🎯 下一步

1. **启动 GUI 应用**
   ```
   .\start-gui.ps1
   ```

2. **登录两个客户端**
   - 用户 101
   - 用户 102

3. **测试文件传输**
   - 选择 Word 文档
   - 观察传输进度
   - 验证接收

4. **检查数据库**
   - 查看 sip_client.db
   - 验证文件记录

## 📚 相关文档

- `WORD_FILE_TRANSFER_GUIDE.md` - 详细的文件传输指南
- `WORD_FILE_TRANSFER_QUICKSTART.md` - 快速开始教程
- `README.md` - 项目总体说明
- `QUICKSTART.md` - 快速启动指南

---

**最后更新**: 2025年11月29日  
**编译状态**: ✅ 成功  
**应用状态**: 🚀 已启动  
**文件传输功能**: ✨ 已集成
