package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Statistics extends BaseEntity {
    private String statType;                // 统计类型: DAILY, WEEKLY, MONTHLY
    private LocalDate statDate;              // 统计日期
    private Long totalUsers;                 // 总用户数
    private Long activeUsers;                // 活跃用户数
    private Long newUsers;                   // 新增用户数
    private Long totalReservations;           // 总预约数
    private Long completedReservations;      // 完成预约数
    private Long cancelledReservations;       // 取消预约数
    private BigDecimal totalRevenue;          // 总收入
    private BigDecimal averageRevenue;       // 平均收入
    private Double occupancyRate;            // 座位占用率
    private Integer totalSeats;              // 总座位数
    private Integer availableSeats;           // 可用座位数
    private Integer occupiedSeats;            // 占用座位数
    private Double averageStudyDuration;      // 平均学习时长
    private Integer peakHourUsage;           // 高峰时段使用量
    private String popularRoomType;           // 热门自习室类型
    private String popularTimeSlot;          // 热门时段
}
