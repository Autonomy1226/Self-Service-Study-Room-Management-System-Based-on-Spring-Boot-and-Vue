package com.studyroom.service.impl;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.StudyRoom;
import com.studyroom.entity.User;
import com.studyroom.entity.Payment;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.mapper.StudyRoomMapper;
import com.studyroom.mapper.UserMapper;
import com.studyroom.mapper.PaymentMapper;
import com.studyroom.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Map<String, Object> getReservationStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Reservation> allReservations = reservationMapper.selectByTimeRange(startTime, endTime);
        
        long pendingCount = allReservations.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        long confirmedCount = allReservations.stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .count();
        long inUseCount = allReservations.stream()
                .filter(r -> "IN_USE".equals(r.getStatus()))
                .count();
        long completedCount = allReservations.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
        long cancelledCount = allReservations.stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .count();
        
        stats.put("totalCount", allReservations.size());
        stats.put("pendingCount", pendingCount);
        stats.put("confirmedCount", confirmedCount);
        stats.put("inUseCount", inUseCount);
        stats.put("completedCount", completedCount);
        stats.put("cancelledCount", cancelledCount);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getRoomUsageStats() {
        List<StudyRoom> rooms = studyRoomMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        
        for (StudyRoom room : rooms) {
            Map<String, Object> roomStat = new HashMap<>();
            roomStat.put("roomId", room.getId());
            roomStat.put("roomName", room.getRoomName());
            roomStat.put("capacity", room.getCapacity());
            
            // 查询该自习室的所有预约（最近7天）
            List<Reservation> roomReservations = reservationMapper.selectByTimeRange(weekAgo, now);
            
            // 计算实际使用时间（分钟）
            long totalUsedMinutes = 0;
            for (Reservation res : roomReservations) {
                // 计算预约时长（分钟）
                long minutes = java.time.Duration.between(res.getStartTime(), res.getEndTime()).toMinutes();
                totalUsedMinutes += minutes;
            }
            
            // 计算总可用时间（分钟）：假设每天开放12小时（8:00-20:00），7天
            long totalAvailableMinutes = room.getCapacity() * 12 * 60 * 7;
            
            // 计算使用率
            double usageRate = totalAvailableMinutes > 0 ? 
                (double) totalUsedMinutes / totalAvailableMinutes * 100 : 0.0;
            
            roomStat.put("reservationCount", roomReservations.size());
            roomStat.put("totalUsedMinutes", totalUsedMinutes);
            roomStat.put("usageRate", Math.round(usageRate * 100.0) / 100.0);
            
            result.add(roomStat);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyReservationTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            
            // 查询当天的预约数量
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            List<Reservation> dayReservations = reservationMapper.selectByTimeRange(dayStart, dayEnd);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dateStr);
            dayStat.put("count", dayReservations.size());
            
            trend.add(dayStat);
            current = current.plusDays(1);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getSeatPopularityRanking(int limit) {
        List<Map<String, Object>> ranking = new ArrayList<>();
        
        // 获取所有预约记录
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        List<Reservation> allReservations = reservationMapper.selectByTimeRange(monthAgo, LocalDateTime.now());
        
        // 统计每个座位的预约次数
        Map<Long, Integer> seatReservationCount = new HashMap<>();
        for (Reservation res : allReservations) {
            seatReservationCount.put(res.getSeatId(), 
                seatReservationCount.getOrDefault(res.getSeatId(), 0) + 1);
        }
        
        // 转换为列表并排序
        List<Map.Entry<Long, Integer>> sortedSeats = new ArrayList<>(seatReservationCount.entrySet());
        sortedSeats.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 取前limit个
        for (int i = 0; i < Math.min(limit, sortedSeats.size()); i++) {
            Map.Entry<Long, Integer> entry = sortedSeats.get(i);
            Map<String, Object> seatStat = new HashMap<>();
            seatStat.put("seatId", entry.getKey());
            seatStat.put("reservationCount", entry.getValue());
            seatStat.put("rank", i + 1);
            
            ranking.add(seatStat);
        }
        
        return ranking;
    }

    @Override
    public Map<String, Object> getUserActivityStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> users = userMapper.selectAll();
        stats.put("totalUsers", users.size());
        
        // 统计活跃用户（最近7天有预约的用户）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Reservation> recentReservations = reservationMapper.selectByTimeRange(weekAgo, LocalDateTime.now());
        Set<Long> activeUserIds = new HashSet<>();
        for (Reservation res : recentReservations) {
            activeUserIds.add(res.getUserId());
        }
        stats.put("activeUsers", activeUserIds.size());
        
        // 统计今日新用户
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        List<User> newUsers = users.stream()
            .filter(user -> user.getCreateTime() != null && 
                   (user.getCreateTime().isAfter(todayStart) && user.getCreateTime().isBefore(todayEnd)))
            .collect(java.util.stream.Collectors.toList());
        stats.put("newUsersToday", newUsers.size());
        
        // 计算平均预约次数
        double avgReservations = activeUserIds.size() > 0 ? 
            (double) recentReservations.size() / activeUserIds.size() : 0.0;
        stats.put("avgReservationsPerUser", Math.round(avgReservations * 100.0) / 100.0);
        
        return stats;
    }

    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<StudyRoom> rooms = studyRoomMapper.selectAll();
        List<Reservation> reservations = reservationMapper.selectAll();
        List<User> users = userMapper.selectAll();
        
        overview.put("totalRooms", rooms.size());
        overview.put("totalReservations", reservations.size());
        overview.put("totalUsers", users.size());
        overview.put("todayReservations", 0);
        
        return overview;
    }

    @Override
    public List<Map<String, Object>> getDailyUserStats(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            
            // 查询当天的新用户数
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            
            List<User> newUsers = userMapper.selectAll().stream()
                .filter(user -> user.getCreateTime() != null && 
                       (user.getCreateTime().isAfter(dayStart) && user.getCreateTime().isBefore(dayEnd)))
                .collect(java.util.stream.Collectors.toList());
            
            // 查询当天的活跃用户数
            List<Reservation> dayReservations = reservationMapper.selectByTimeRange(dayStart, dayEnd);
            Set<Long> activeUserIds = new HashSet<>();
            for (Reservation res : dayReservations) {
                activeUserIds.add(res.getUserId());
            }
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dateStr);
            dayStat.put("newUsers", newUsers.size());
            dayStat.put("activeUsers", activeUserIds.size());
            dayStat.put("totalReservations", dayReservations.size());
            
            stats.add(dayStat);
            current = current.plusDays(1);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getHourlyStats(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        // 按小时分组统计
        Map<Integer, Integer> hourlyCount = new HashMap<>();
        
        // 获取时间范围内的所有预约
        List<Reservation> reservations = reservationMapper.selectByTimeRange(startTime, endTime);
        
        for (Reservation res : reservations) {
            // 统计预约开始时间的小时
            int hour = res.getStartTime().getHour();
            hourlyCount.put(hour, hourlyCount.getOrDefault(hour, 0) + 1);
        }
        
        // 生成8:00-22:00的统计数据
        for (int hour = 8; hour <= 22; hour++) {
            Map<String, Object> hourStat = new HashMap<>();
            hourStat.put("hour", hour);
            hourStat.put("hourStr", String.format("%02d:00", hour));
            hourStat.put("count", hourlyCount.getOrDefault(hour, 0));
            stats.add(hourStat);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getRevenueStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取时间范围内的所有支付记录
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Payment> payments = paymentMapper.selectByTimeRange(startDateTime, endDateTime);
        
        // 计算总收入（排除退款记录）
        double totalRevenue = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getAmount().doubleValue() > 0)
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();
        
        // 按支付方式统计（排除退款记录）
        Map<String, Double> revenueByMethod = new HashMap<>();
        Map<String, Long> countByMethod = new HashMap<>();
        
        for (Payment payment : payments) {
            if ("SUCCESS".equals(payment.getStatus()) && payment.getAmount().doubleValue() > 0) {
                String method = payment.getPaymentMethod();
                revenueByMethod.put(method, revenueByMethod.getOrDefault(method, 0.0) + payment.getAmount().doubleValue());
                countByMethod.put(method, countByMethod.getOrDefault(method, 0L) + 1);
            }
        }
        
        // 计算平均收入（排除退款记录）
        long successCount = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getAmount().doubleValue() > 0)
                .count();
        double avgRevenue = successCount > 0 ? totalRevenue / successCount : 0.0;
        
        stats.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        stats.put("avgRevenue", Math.round(avgRevenue * 100.0) / 100.0);
        stats.put("totalTransactions", successCount);
        stats.put("revenueByMethod", revenueByMethod);
        stats.put("countByMethod", countByMethod);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            
            // 查询当天的支付记录
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            
            List<Payment> dayPayments = paymentMapper.selectByTimeRange(dayStart, dayEnd);
            
            // 计算当天收入（排除退款记录）
            double dayRevenue = dayPayments.stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getAmount().doubleValue() > 0)
                    .mapToDouble(p -> p.getAmount().doubleValue())
                    .sum();
            
            // 计算当天交易数（排除退款记录）
            long dayTransactions = dayPayments.stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getAmount().doubleValue() > 0)
                    .count();
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dateStr);
            dayStat.put("revenue", Math.round(dayRevenue * 100.0) / 100.0);
            dayStat.put("transactions", dayTransactions);
            
            dailyStats.add(dayStat);
            current = current.plusDays(1);
        }
        
        return dailyStats;
    }

    @Override
    public List<Map<String, Object>> getPaymentMethodStats(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Payment> payments = paymentMapper.selectByTimeRange(startDateTime, endDateTime);
        
        // 按支付方式分组统计（排除退款记录）
        Map<String, Double> revenueByMethod = new HashMap<>();
        Map<String, Long> countByMethod = new HashMap<>();
        Map<String, Long> successCountByMethod = new HashMap<>();
        
        for (Payment payment : payments) {
            String method = payment.getPaymentMethod();
            
            // 总交易数（包括退款）
            countByMethod.put(method, countByMethod.getOrDefault(method, 0L) + 1);
            
            // 成功交易数和收入（排除退款记录）
            if ("SUCCESS".equals(payment.getStatus()) && payment.getAmount().doubleValue() > 0) {
                revenueByMethod.put(method, revenueByMethod.getOrDefault(method, 0.0) + payment.getAmount().doubleValue());
                successCountByMethod.put(method, successCountByMethod.getOrDefault(method, 0L) + 1);
            }
        }
        
        // 生成统计数据
        for (String method : Arrays.asList("ALIPAY", "WECHAT", "BALANCE")) {
            Map<String, Object> methodStat = new HashMap<>();
            methodStat.put("method", method);
            methodStat.put("methodName", getPaymentMethodName(method));
            methodStat.put("revenue", revenueByMethod.getOrDefault(method, 0.0));
            methodStat.put("totalTransactions", countByMethod.getOrDefault(method, 0L));
            methodStat.put("successTransactions", successCountByMethod.getOrDefault(method, 0L));
            
            // 计算成功率
            long total = countByMethod.getOrDefault(method, 0L);
            long success = successCountByMethod.getOrDefault(method, 0L);
            double successRate = total > 0 ? (double) success / total * 100 : 0.0;
            methodStat.put("successRate", Math.round(successRate * 100.0) / 100.0);
            
            stats.add(methodStat);
        }
        
        return stats;
    }
    
    private String getPaymentMethodName(String method) {
        switch (method) {
            case "ALIPAY": return "支付宝";
            case "WECHAT": return "微信支付";
            case "BALANCE": return "余额支付";
            default: return method;
        }
    }
}
