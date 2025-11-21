package com.example.admin.service;

import com.example.sipclient.config.SipConfig;
import com.example.sipclient.sip.SipUserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIP 服务
 * 管理所有用户的 SIP 连接
 */
@Service
public class SipService {
    
    private static final Logger logger = LoggerFactory.getLogger(SipService.class);
    
    // 存储每个用户的 SipUserAgent 实例
    private final Map<String, SipUserAgent> userAgents = new ConcurrentHashMap<>();
    
    /**
     * 注册 SIP 用户
     * @param sipUri SIP URI
     * @param password 密码
     * @param localIp 本地 IP
     * @param localPort 本地端口
     * @return SipUserAgent 实例
     * @throws Exception 注册失败
     */
    public SipUserAgent register(String sipUri, String password, String localIp, int localPort) throws Exception {
        logger.info("开始注册 SIP 用户: {}", sipUri);
        
        // 检查是否已经注册
        if (userAgents.containsKey(sipUri)) {
            logger.info("用户已经注册，返回现有连接: {}", sipUri);
            return userAgents.get(sipUri);
        }
        
        // 创建 SIP 配置
        SipConfig config = new SipConfig();
        config.setSipUri(sipUri);
        config.setPassword(password);
        config.setLocalIp(localIp);
        config.setLocalPort(localPort);
        
        // 创建并初始化 SipUserAgent
        SipUserAgent userAgent = new SipUserAgent(config);
        userAgent.start();
        
        // 等待注册完成（最多等待5秒）
        int retries = 50;
        while (retries > 0 && !userAgent.isRegistered()) {
            Thread.sleep(100);
            retries--;
        }
        
        if (!userAgent.isRegistered()) {
            userAgent.stop();
            throw new Exception("SIP 注册超时，请检查网络和服务器配置");
        }
        
        // 保存到映射表
        userAgents.put(sipUri, userAgent);
        logger.info("SIP 用户注册成功: {}", sipUri);
        
        return userAgent;
    }
    
    /**
     * 注销 SIP 用户
     * @param sipUri SIP URI
     */
    public void unregister(String sipUri) {
        SipUserAgent userAgent = userAgents.remove(sipUri);
        if (userAgent != null) {
            try {
                userAgent.unregister();
                userAgent.stop();
                logger.info("SIP 用户注销成功: {}", sipUri);
            } catch (Exception e) {
                logger.error("注销 SIP 用户失败: {}", sipUri, e);
            }
        }
    }
    
    /**
     * 获取用户的 SipUserAgent
     * @param sipUri SIP URI
     * @return SipUserAgent 实例，如果不存在返回 null
     */
    public SipUserAgent getUserAgent(String sipUri) {
        return userAgents.get(sipUri);
    }
    
    /**
     * 检查用户是否已注册
     * @param sipUri SIP URI
     * @return true 如果已注册
     */
    public boolean isRegistered(String sipUri) {
        SipUserAgent userAgent = userAgents.get(sipUri);
        return userAgent != null && userAgent.isRegistered();
    }
    
    /**
     * 发送消息
     * @param fromSipUri 发送者 SIP URI
     * @param toSipUri 接收者 SIP URI
     * @param content 消息内容
     * @throws Exception 发送失败
     */
    public void sendMessage(String fromSipUri, String toSipUri, String content) throws Exception {
        SipUserAgent userAgent = userAgents.get(fromSipUri);
        if (userAgent == null) {
            throw new Exception("用户未登录: " + fromSipUri);
        }
        
        userAgent.sendMessage(toSipUri, content);
        logger.info("消息发送成功: {} -> {}", fromSipUri, toSipUri);
    }
    
    /**
     * 发起呼叫
     * @param fromSipUri 发起者 SIP URI
     * @param toSipUri 被叫者 SIP URI
     * @throws Exception 呼叫失败
     */
    public void initiateCall(String fromSipUri, String toSipUri) throws Exception {
        SipUserAgent userAgent = userAgents.get(fromSipUri);
        if (userAgent == null) {
            throw new Exception("用户未登录: " + fromSipUri);
        }
        
        userAgent.initiateCall(toSipUri);
        logger.info("呼叫发起成功: {} -> {}", fromSipUri, toSipUri);
    }
    
    /**
     * 挂断呼叫
     * @param sipUri SIP URI
     * @param peerUri 对方 SIP URI
     * @throws Exception 挂断失败
     */
    public void hangupCall(String sipUri, String peerUri) throws Exception {
        SipUserAgent userAgent = userAgents.get(sipUri);
        if (userAgent == null) {
            throw new Exception("用户未登录: " + sipUri);
        }
        
        userAgent.hangup(peerUri);
        logger.info("呼叫挂断成功: {} -> {}", sipUri, peerUri);
    }
    
    /**
     * 应用关闭时清理所有连接
     */
    @PreDestroy
    public void cleanup() {
        logger.info("清理所有 SIP 连接...");
        userAgents.forEach((sipUri, userAgent) -> {
            try {
                userAgent.unregister();
                userAgent.stop();
            } catch (Exception e) {
                logger.error("清理 SIP 连接失败: {}", sipUri, e);
            }
        });
        userAgents.clear();
    }
}
