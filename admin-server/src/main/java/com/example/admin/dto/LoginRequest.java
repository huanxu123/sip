package com.example.admin.dto;

/**
 * 登录请求 DTO
 */
public class LoginRequest {
    
    private String sipUri;
    private String password;
    private String localIp;
    private Integer localPort;
    
    public String getSipUri() {
        return sipUri;
    }
    
    public void setSipUri(String sipUri) {
        this.sipUri = sipUri;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getLocalIp() {
        return localIp;
    }
    
    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }
    
    public Integer getLocalPort() {
        return localPort;
    }
    
    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
}
