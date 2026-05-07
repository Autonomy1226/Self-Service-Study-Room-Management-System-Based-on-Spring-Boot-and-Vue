package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/reservation-stats")
    public Result<Map<String, Object>> getReservationStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(dashboardService.getReservationStats(startTime, endTime));
    }

    @GetMapping("/room-usage")
    public Result<List<Map<String, Object>>> getRoomUsageStats() {
        return Result.success(dashboardService.getRoomUsageStats());
    }

    @GetMapping("/daily-trend")
    public Result<List<Map<String, Object>>> getDailyReservationTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getDailyReservationTrend(startDate, endDate));
    }

    @GetMapping("/seat-popularity")
    public Result<List<Map<String, Object>>> getSeatPopularityRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(dashboardService.getSeatPopularityRanking(limit));
    }

    @GetMapping("/user-activity")
    public Result<Map<String, Object>> getUserActivityStats() {
        return Result.success(dashboardService.getUserActivityStats());
    }

    @GetMapping("/daily-user-stats")
    public Result<List<Map<String, Object>>> getDailyUserStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getDailyUserStats(startDate, endDate));
    }

    @GetMapping("/hourly-stats")
    public Result<List<Map<String, Object>>> getHourlyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(dashboardService.getHourlyStats(startTime, endTime));
    }

    @GetMapping("/revenue-stats")
    public Result<Map<String, Object>> getRevenueStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getRevenueStats(startDate, endDate));
    }

    @GetMapping("/daily-revenue")
    public Result<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getDailyRevenue(startDate, endDate));
    }

    @GetMapping("/payment-method-stats")
    public Result<List<Map<String, Object>>> getPaymentMethodStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getPaymentMethodStats(startDate, endDate));
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        return Result.success(dashboardService.getSystemOverview());
    }
}
