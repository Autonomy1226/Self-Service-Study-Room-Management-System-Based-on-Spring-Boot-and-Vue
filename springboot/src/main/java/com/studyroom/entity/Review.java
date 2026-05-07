package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Review extends BaseEntity {
    private Long userId;
    private Long seatId;
    private Long reservationId;
    private Integer rating; // 1-5星评分
    private String content; // 评价内容
    private String tags; // 标签
    private Boolean isAnonymous; // 是否匿名
    private String status; // APPROVED-已通过, PENDING-待审核
    private Integer helpfulCount; // 有用数
}
