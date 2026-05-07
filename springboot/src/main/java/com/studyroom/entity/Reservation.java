package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Reservation extends BaseEntity {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "座位ID不能为空")
    private Long seatId;
    
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是将来时间")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是将来时间")
    private LocalDateTime endTime;
    
    @NotNull(message = "总价不能为空")
    @DecimalMin(value = "0.0", message = "总价不能小于0")
    @DecimalMax(value = "999999.99", message = "总价不能超过999999.99")
    private BigDecimal totalPrice;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(PENDING|IN_USE|COMPLETED|CANCELLED)$", 
            message = "状态只能是PENDING、IN_USE、COMPLETED或CANCELLED")
    private String status;
    
    // 新增字段
    private String reservationType;         // 预约类型: INDIVIDUAL, GROUP
    private Integer participantCount;       // 参与人数
    private String specialRequirements;     // 特殊要求
    private String paymentMethod;            // 支付方式
    private String paymentStatus;           // 支付状态
    private LocalDateTime paymentTime;      // 支付时间
    private LocalDateTime checkInTime;      // 签到时间
    private LocalDateTime checkOutTime;    // 签退时间
    private Boolean isAutoRenew;           // 是否自动续约
    private Integer renewCount;             // 续约次数
    private String cancelReason;            // 取消原因
    private Integer cancelPenalty;         // 取消退款金额
    private String qrCode;                  // 预约二维码
    private Integer reminderSent;          // 提醒发送次数
    private LocalDateTime lastReminderTime; // 最后提醒时间
    private Double rating;                  // 用户评分
    private String review;                  // 用户评价
    private LocalDateTime reviewTime;      // 评价时间
}