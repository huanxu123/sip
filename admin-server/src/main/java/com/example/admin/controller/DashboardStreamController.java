package com.example.admin.controller;

import com.example.admin.entity.DashboardSnapshot;
import com.example.admin.service.DashboardSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/stream")
public class DashboardStreamController {

    private static final Logger log = LoggerFactory.getLogger(DashboardStreamController.class);
    private static final long SSE_TIMEOUT_MS = Duration.ofMinutes(15).toMillis();

    private final DashboardSnapshotService snapshotService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardStreamController(DashboardSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> {
            emitters.remove(emitter);
            log.debug("SSE connection closed because of error: {}", error.getMessage());
        });

        sendSnapshot(emitter);
        return emitter;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledPush() {
        DashboardSnapshot snapshot = snapshotService.capture();
        emitters.forEach(emitter -> sendSnapshot(emitter, snapshot));
    }

    private void sendSnapshot(SseEmitter emitter) {
        sendSnapshot(emitter, snapshotService.capture());
    }

    private void sendSnapshot(SseEmitter emitter, DashboardSnapshot snapshot) {
        try {
            emitter.send(SseEmitter.event()
                    .name("dashboard")
                    .data(snapshot));
        } catch (IOException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
            log.debug("Removed SSE emitter after failure: {}", e.getMessage());
        }
    }
}
