package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeatRecommendation extends BaseEntity {
    private Long userId;
    private Long seatId;
    private Long roomId;
    private String recommendationType;      // 推荐类型: PREFERENCE, POPULAR, AVAILABLE
    private BigDecimal score;              // 推荐分数
    private String reason;                 // 推荐理由
    private LocalDateTime recommendTime;   // 推荐时间
    private Boolean isAccepted;            // 是否被接受
    private LocalDateTime acceptTime;      // 接受时间
}
