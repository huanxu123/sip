package com.example.admin.controller;

import com.example.admin.entity.DashboardSnapshot;
import com.example.admin.service.DashboardSnapshotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardSnapshotService snapshotService;

    public DashboardController(DashboardSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @GetMapping
    public DashboardSnapshot snapshot() {
        return snapshotService.capture();
    }
}
