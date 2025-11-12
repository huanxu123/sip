package com.example.admin.entity;

import java.time.Instant;
import java.util.List;

public record DashboardSnapshot(
        StatsSummary stats,
        List<UserSummary> users,
        List<CallRecord> calls,
        Instant generatedAt
) {}
