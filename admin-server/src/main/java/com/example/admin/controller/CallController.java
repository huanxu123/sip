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
 * 呼叫控制器
 * 处理呼叫相关请求
 */
@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
public class CallController {
    
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);
    
    @Autowired
    private SipService sipService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 发起呼叫
     * POST /api/calls
     */
    @PostMapping
    public ApiResponse<StartCallResponse> startCall(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody StartCallRequest request) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String fromSipUri = jwtUtil.getUserIdFromToken(token);
            
            // 发起呼叫
            sipService.initiateCall(fromSipUri, request.getTo());
            
            // 构建响应
            StartCallResponse response = new StartCallResponse();
            response.setCallId(generateCallId());
            response.setState("INITIATED");
            response.setTimestamp(System.currentTimeMillis());
            
            logger.info("呼叫发起成功: {} -> {}", fromSipUri, request.getTo());
            return ApiResponse.success(response, "呼叫发起成功");
            
        } catch (Exception e) {
            logger.error("呼叫发起失败", e);
            return ApiResponse.error("呼叫发起失败: " + e.getMessage());
        }
    }
    
    /**
     * 接听呼叫
     * PUT /api/calls/{callId}/answer
     */
    @PutMapping("/{callId}/answer")
    public ApiResponse<Void> answerCall(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String callId) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 实现接听呼叫逻辑
            logger.info("接听呼叫: {} - {}", userId, callId);
            
            return ApiResponse.success(null, "接听成功");
            
        } catch (Exception e) {
            logger.error("接听呼叫失败", e);
            return ApiResponse.error("接听呼叫失败: " + e.getMessage());
        }
    }
    
    /**
     * 拒绝呼叫
     * PUT /api/calls/{callId}/reject
     */
    @PutMapping("/{callId}/reject")
    public ApiResponse<Void> rejectCall(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String callId) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 实现拒绝呼叫逻辑
            logger.info("拒绝呼叫: {} - {}", userId, callId);
            
            return ApiResponse.success(null, "拒绝成功");
            
        } catch (Exception e) {
            logger.error("拒绝呼叫失败", e);
            return ApiResponse.error("拒绝呼叫失败: " + e.getMessage());
        }
    }
    
    /**
     * 挂断呼叫
     * DELETE /api/calls/{callId}
     */
    @DeleteMapping("/{callId}")
    public ApiResponse<Void> hangupCall(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String callId,
            @RequestParam(required = false) String peerUri) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // 挂断呼叫
            if (peerUri != null) {
                sipService.hangupCall(userId, peerUri);
            }
            
            logger.info("挂断呼叫: {} - {}", userId, callId);
            return ApiResponse.success(null, "挂断成功");
            
        } catch (Exception e) {
            logger.error("挂断呼叫失败", e);
            return ApiResponse.error("挂断呼叫失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取呼叫状态
     * GET /api/calls/{callId}
     */
    @GetMapping("/{callId}")
    public ApiResponse<CallStatus> getCallStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String callId) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 从缓存或数据库获取呼叫状态
            logger.info("获取呼叫状态: {} - {}", userId, callId);
            
            CallStatus status = new CallStatus();
            status.setCallId(callId);
            status.setState("UNKNOWN");
            
            return ApiResponse.success(status);
            
        } catch (Exception e) {
            logger.error("获取呼叫状态失败", e);
            return ApiResponse.error("获取呼叫状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取通话历史
     * GET /api/calls/history
     */
    @GetMapping("/history")
    public ApiResponse<List<CallRecord>> getCallHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return ApiResponse.error("无效的 Authorization 头");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // TODO: 从数据库获取通话历史
            logger.info("获取通话历史: {}", userId);
            
            return ApiResponse.success(List.of());
            
        } catch (Exception e) {
            logger.error("获取通话历史失败", e);
            return ApiResponse.error("获取通话历史失败: " + e.getMessage());
        }
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private String generateCallId() {
        return "call_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    // DTO 类
    public static class StartCallRequest {
        private String to;
        private String type;
        
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
    }
    
    public static class StartCallResponse {
        private String callId;
        private String state;
        private Long timestamp;
        
        public String getCallId() {
            return callId;
        }
        
        public void setCallId(String callId) {
            this.callId = callId;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
    
    public static class CallStatus {
        private String callId;
        private String state;
        private String direction;
        private Long startTime;
        private Long duration;
        
        public String getCallId() {
            return callId;
        }
        
        public void setCallId(String callId) {
            this.callId = callId;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public String getDirection() {
            return direction;
        }
        
        public void setDirection(String direction) {
            this.direction = direction;
        }
        
        public Long getStartTime() {
            return startTime;
        }
        
        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }
        
        public Long getDuration() {
            return duration;
        }
        
        public void setDuration(Long duration) {
            this.duration = duration;
        }
    }
    
    public static class CallRecord {
        private String callId;
        private String peerId;
        private String peerName;
        private String direction;
        private Long startTime;
        private Long duration;
        private String status;
        
        // Getters and Setters
    }
}
