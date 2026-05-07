package com.studyroom.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    // 获取预约统计数据
    Map<String, Object> getReservationStats(LocalDateTime startTime, LocalDateTime endTime);
    // 获取自习室使用率
    List<Map<String, Object>> getRoomUsageStats();
    // 获取每日预约趋势
    List<Map<String, Object>> getDailyReservationTrend(LocalDate startDate, LocalDate endDate);
    // 获取座位热度排行
    List<Map<String, Object>> getSeatPopularityRanking(int limit);
    // 获取用户活跃度统计
    Map<String, Object> getUserActivityStats();
    // 获取每日用户统计
    List<Map<String, Object>> getDailyUserStats(LocalDate startDate, LocalDate endDate);
    // 获取小时统计数据
    List<Map<String, Object>> getHourlyStats(LocalDateTime startTime, LocalDateTime endTime);
    // 获取收入统计
    Map<String, Object> getRevenueStats(LocalDate startDate, LocalDate endDate);
    // 获取每日收入趋势
    List<Map<String, Object>> getDailyRevenue(LocalDate startDate, LocalDate endDate);
    // 获取支付方式统计
    List<Map<String, Object>> getPaymentMethodStats(LocalDate startDate, LocalDate endDate);
    // 获取系统概览数据
    Map<String, Object> getSystemOverview();
}
