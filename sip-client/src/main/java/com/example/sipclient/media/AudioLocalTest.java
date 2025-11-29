package com.example.sipclient.media;

public class AudioLocalTest {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建你刚才写的音频会话对象
        AudioSession session = new AudioSession();

        System.out.println(">>> 测试开始：请对着麦克风说话，你应该能听到回音 <<<");

        // 2. 启动通话！
        // 参数含义：目标IP(自己), 目标端口(55555), 本地监听端口(55555)
        // 这样声音发出去又会发回给自己
        session.start("127.0.0.1", 55555, 55555);

        // 3. 让程序运行 15 秒，让你有时间说话
        Thread.sleep(15000);

        // 4. 停止测试
        session.stop();
        System.out.println(">>> 测试结束 <<<");
    }
}