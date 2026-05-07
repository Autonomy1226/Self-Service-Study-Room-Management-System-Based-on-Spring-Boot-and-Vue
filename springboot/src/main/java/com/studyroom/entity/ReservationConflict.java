package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReservationConflict extends BaseEntity {
    private Long reservationId1;            // 冲突预约1
    private Long reservationId2;            // 冲突预约2
    private Long seatId;                    // 座位ID
    private String conflictType;            // 冲突类型: TIME_OVERLAP, DOUBLE_BOOKING
    private LocalDateTime conflictTime;     // 冲突时间
    private String resolution;              // 解决方案
    private Boolean isResolved;            // 是否已解决
    private Long resolvedBy;                // 解决人ID
    private LocalDateTime resolveTime;      // 解决时间
}
