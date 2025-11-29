package com.example.sipclient.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.net.*;

/**
 * 成员 A 实现的真实音频会话
 * 实现了 RTP 音频采集与播放
 */
public class AudioSession implements MediaSession {

    private static final Logger log = LoggerFactory.getLogger(AudioSession.class);

    // 控制开关
    private volatile boolean running = false;

    // 网络与音频组件
    private DatagramSocket socket;
    private final AudioFormat format = new AudioFormat(8000, 16, 1, true, true); // 8k采样, 16位, 单声道

    // 对方的地址信息
    private String remoteIp;
    private int remotePort;

    /**
     * 这是接口要求的默认启动方法。
     * 但因为没有对方IP，我们没法在这里真正启动通话。
     * 所以留空或打印提示。
     */
    @Override
    public void start() {
        log.warn("请调用带参数的 start(ip, port, localPort) 来启动真实通话");
    }

    /**
     * ✅ 这是你需要调用的真实启动方法
     * @param targetIp 对方IP
     * @param targetPort 对方端口
     * @param localPort 本地监听端口
     */
    public void start(String targetIp, int targetPort, int localPort) {
        if (running) return;
        this.remoteIp = targetIp;
        this.remotePort = targetPort;
        this.running = true;

        try {
            // 1. 准备网络
            socket = new DatagramSocket(localPort);
            log.info("音频会话启动，本地端口: {}, 目标: {}:{}", localPort, targetIp, targetPort);

            // 2. 开启发送线程（麦克风 -> 网络）
            new Thread(this::captureAndSend, "Audio-Sender").start();

            // 3. 开启接收线程（网络 -> 扬声器）
            new Thread(this::receiveAndPlay, "Audio-Receiver").start();

        } catch (SocketException e) {
            log.error("启动失败: {}", e.getMessage());
            running = false;
        }
    }

    @Override
    public void stop() {
        if (!running) return;
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        log.info("音频会话已停止");
    }


    public boolean isRunning() {
        return running;
    }

    // --- 内部逻辑：采集麦克风并发送 ---
    private void captureAndSend() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();

            byte[] buffer = new byte[1024];
            InetAddress address = InetAddress.getByName(remoteIp);

            log.info("麦克风已开启...");
            while (running) {
                int bytesRead = mic.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, remotePort);
                    socket.send(packet);
                }
            }
            mic.close();
        } catch (Exception e) {
            log.error("麦克风采集异常: ", e);
        }
    }

    // --- 内部逻辑：接收网络数据并播放 ---
    private void receiveAndPlay() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            log.info("扬声器已就绪...");
            while (running) {
                try {
                    socket.receive(packet);
                    speaker.write(packet.getData(), 0, packet.getLength());
                } catch (SocketException e) {
                    // Socket关闭时会抛出此异常，正常退出即可
                    break;
                }
            }
            speaker.close();
        } catch (Exception e) {
            log.error("播放异常: ", e);
        }
    }
}
