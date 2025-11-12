package com.example.admin.service;

import com.example.admin.entity.CallRecord;
import com.example.admin.entity.DashboardSnapshot;
import com.example.admin.entity.StatsSummary;
import com.example.admin.entity.UserSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DashboardSnapshotService {

    private final StatsService statsService;
    private final UserService userService;
    private final CallRecordService callRecordService;

    public DashboardSnapshotService(
            StatsService statsService,
            UserService userService,
            CallRecordService callRecordService
    ) {
        this.statsService = statsService;
        this.userService = userService;
        this.callRecordService = callRecordService;
    }

    public DashboardSnapshot capture() {
        StatsSummary stats = statsService.snapshot();
        List<UserSummary> users = userService.listUsers();
        List<CallRecord> calls = callRecordService.listCallRecords();
        return new DashboardSnapshot(stats, users, calls, Instant.now());
    }
}
