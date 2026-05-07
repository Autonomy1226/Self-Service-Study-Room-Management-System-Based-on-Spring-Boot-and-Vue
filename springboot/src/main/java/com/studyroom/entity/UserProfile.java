package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity {
    private Long userId;
    
    // 用户统计信息
    private Integer totalReservations;      // 总预约次数
    private Integer completedReservations;  // 完成预约次数
    private Integer cancelledReservations;  // 取消预约次数
    private BigDecimal totalSpending;       // 总消费金额
    private Integer totalStudyHours;        // 总学习时长(小时)
    
    // 用户行为特征
    private String preferredRoomType;      // 偏好自习室类型
    private String preferredTimeSlot;      // 偏好时间段
    private Integer averageStudyDuration;   // 平均学习时长(分钟)
    private Integer favoriteSeatId;         // 常用座位ID
    
    // 信用评分
    private Integer creditScore;            // 信用分数 (0-100)
    private String creditLevel;             // 信用等级
    private LocalDateTime lastViolationTime; // 最后违规时间
    
    // 会员信息
    private String memberLevel;             // 会员等级
    private LocalDateTime memberExpireTime; // 会员到期时间
    private BigDecimal discountRate;        // 折扣率
    
    // 学习统计
    private Integer weeklyStudyDays;        // 本周学习天数
    private Integer monthlyStudyDays;       // 本月学习天数
    private LocalDateTime lastStudyTime;   // 最后学习时间
    private Integer consecutiveStudyDays;   // 连续学习天数
}
