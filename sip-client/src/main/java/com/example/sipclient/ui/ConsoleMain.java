package com.example.sipclient.ui;

import com.example.sipclient.call.CallManager;
import com.example.sipclient.chat.MessageHandler;
import com.example.sipclient.sip.SipUserAgent;

import java.time.Duration;
import java.util.Scanner;
import javax.sip.SipException;

/**
 * Minimal console UI to drive SipUserAgent for manual testing.
 *
 * Usage: run this main, provide SIP URI (sip:alice@example.com), password, local IP and port.
 */
public class ConsoleMain {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Project SIP Client - Console ===");
        System.out.print("SIP URI (e.g. sip:alice@example.com): ");
        String uri = sc.nextLine().trim();
        System.out.print("Password: ");
        String pwd = sc.nextLine().trim();
        System.out.print("Local IP (reachable by server): ");
        String localIp = sc.nextLine().trim();
        System.out.print("Local UDP port (e.g. 5070): ");
        int localPort = Integer.parseInt(sc.nextLine().trim());

        System.out.println("Starting SIP user agent...");
        SipUserAgent ua = new SipUserAgent(uri, pwd, localIp, localPort);

        // simple handlers to print incoming events
        ua.setMessageHandler(new MessageHandler() {
            @Override
            public void handleIncomingMessage(String from, String body) {
                System.out.println("[INCOMING MESSAGE] from=" + from + " body=" + body);
            }
        });

        ua.setCallManager(new CallManager());

        System.out.println("Registering (10s timeout)...");
        boolean ok = ua.register(Duration.ofSeconds(10));
        System.out.println("Registered: " + ok + " (state=" + ua.isRegistered() + ")");

        printHelp();

        loop:
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            String[] parts = line.split(" ", 3);
            String cmd = parts[0].trim();
            try {
                switch (cmd) {
                    case "help":
                        printHelp();
                        break;
                    case "msg":
                        if (parts.length < 3) {
                            System.out.println("usage: msg <sip:target@host> <text>");
                            break;
                        }
                        ua.sendMessage(parts[1].trim(), parts[2]);
                        System.out.println("Message sent.");
                        break;
                    case "call":
                        if (parts.length < 2) {
                            System.out.println("usage: call <sip:target@host>");
                            break;
                        }
                        ua.startCall(parts[1].trim());
                        System.out.println("INVITE sent to " + parts[1].trim());
                        break;
                    case "hangup":
                        if (parts.length < 2) {
                            System.out.println("usage: hangup <sip:target@host>");
                            break;
                        }
                        ua.hangup(parts[1].trim());
                        System.out.println("Hangup requested.");
                        break;
                    case "unregister":
                        System.out.println("Unregistering...");
                        ua.unregister(Duration.ofSeconds(5));
                        System.out.println("Unregistered.");
                        break;
                    case "exit":
                        break loop;
                    default:
                        System.out.println("Unknown command. Type 'help' for commands.");
                }
            } catch (Exception e) {
                System.err.println("Command failed: " + e.getMessage());
            }
        }

        System.out.println("Shutting down...");
        try {
            if (ua.isRegistered()) {
                ua.unregister(Duration.ofSeconds(3));
            }
        } catch (Exception ignored) {
        }
        ua.shutdown();
        sc.close();
        System.out.println("Stopped");
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help                       - show this help");
        System.out.println("  msg <sip:target> <text>    - send a SIP MESSAGE");
        System.out.println("  call <sip:target>          - send INVITE");
        System.out.println("  hangup <sip:target>        - send BYE (if dialog exists)");
        System.out.println("  unregister                 - unregister and remove contact");
        System.out.println("  exit                       - exit program");
    }

}
