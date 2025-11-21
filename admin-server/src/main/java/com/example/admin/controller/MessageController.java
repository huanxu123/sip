package com.example.admin.controller;

import com.example.admin.dto.ApiResponse;
import com.example.admin.service.SipService;
import com.example.admin.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 * 处理消息相关请求
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    
    @Autowired
    private SipService sipService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 发送消息
     * POST /api/messages
     */
    @PostMapping
    public ApiResponse<SendMessageResponse> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SendMessageRequest request) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String fromSipUri = jwtUtil.getUserIdFromToken(token);
            
            // 发送消息
            sipService.sendMessage(fromSipUri, request.getTo(), request.getContent());
            
            // 构建响应
            SendMessageResponse response = new SendMessageResponse();
            response.setMessageId(generateMessageId());
            response.setTimestamp(System.currentTimeMillis());
            response.setStatus("SENT");
            
            logger.info("消息发送成功: {} -> {}", fromSipUri, request.getTo());
            return ApiResponse.success(response, "消息发送成功");
            
        } catch (Exception e) {
            logger.error("消息发送失败", e);
            return ApiResponse.error("消息发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取会话列表
     * GET /api/messages/sessions
     */
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getSessions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 从数据库或缓存获取会话列表
            // 目前返回空列表
            logger.info("获取会话列表: {}", userId);
            return ApiResponse.success(List.of());
            
        } catch (Exception e) {
            logger.error("获取会话列表失败", e);
            return ApiResponse.error("获取会话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取会话历史
     * GET /api/messages/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<SessionHistory> getSessionHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 从数据库获取会话历史
            logger.info("获取会话历史: {} - {}", userId, sessionId);
            
            SessionHistory history = new SessionHistory();
            history.setMessages(List.of());
            history.setTotal(0);
            history.setPage(page);
            history.setPageSize(size);
            
            return ApiResponse.success(history);
            
        } catch (Exception e) {
            logger.error("获取会话历史失败", e);
            return ApiResponse.error("获取会话历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记消息已读
     * PUT /api/messages/{messageId}/read
     */
    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String messageId) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 更新消息状态为已读
            logger.info("标记消息已读: {} - {}", userId, messageId);
            
            return ApiResponse.success(null, "标记成功");
            
        } catch (Exception e) {
            logger.error("标记消息已读失败", e);
            return ApiResponse.error("标记消息已读失败: " + e.getMessage());
        }
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    // DTO 类
    public static class SendMessageRequest {
        private String to;
        private String type;
        private String content;
        
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    public static class SendMessageResponse {
        private String messageId;
        private Long timestamp;
        private String status;
        
        public String getMessageId() {
            return messageId;
        }
        
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    public static class ChatSession {
        private String sessionId;
        private String peerId;
        private String peerName;
        private String lastMessage;
        private Long lastMessageTime;
        private Integer unreadCount;
        
        // Getters and Setters
    }
    
    public static class SessionHistory {
        private List<Message> messages;
        private Integer total;
        private Integer page;
        private Integer pageSize;
        
        public List<Message> getMessages() {
            return messages;
        }
        
        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
        
        public Integer getTotal() {
            return total;
        }
        
        public void setTotal(Integer total) {
            this.total = total;
        }
        
        public Integer getPage() {
            return page;
        }
        
        public void setPage(Integer page) {
            this.page = page;
        }
        
        public Integer getPageSize() {
            return pageSize;
        }
        
        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }
    
    public static class Message {
        private String messageId;
        private String from;
        private String to;
        private String content;
        private Long timestamp;
        private String status;
        
        // Getters and Setters
    }
}
