package com.example.admin.dto;

/**
 * 登录响应 DTO
 */
public class LoginResponse {
    
    private String token;
    private String userId;
    private String displayName;
    private Long expiresIn;
    
    public LoginResponse() {
    }
    
    public LoginResponse(String token, String userId, String displayName, Long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.displayName = displayName;
        this.expiresIn = expiresIn;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
