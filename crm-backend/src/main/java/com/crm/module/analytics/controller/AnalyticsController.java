package com.crm.module.analytics.controller;

import com.crm.common.Result;
import com.crm.module.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = analyticsService.getDashboardStats();
        return Result.ok(stats);
    }

    @GetMapping("/analytics/sales-trend")
    public Result<List<Map<String, Object>>> getSalesTrend() {
        List<Map<String, Object>> trend = analyticsService.getSalesTrend();
        return Result.ok(trend);
    }
}
