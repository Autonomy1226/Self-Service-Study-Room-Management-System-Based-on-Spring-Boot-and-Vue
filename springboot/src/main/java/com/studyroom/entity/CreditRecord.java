package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreditRecord extends BaseEntity {
    private Long userId;
    private String recordType;              // 记录类型: INCREASE, DECREASE
    private Integer scoreChange;             // 分数变化
    private Integer scoreAfter;              // 变化后分数
    private String reason;                   // 原因
    private String relatedType;              // 关联类型: RESERVATION, USAGE, VIOLATION
    private Long relatedId;                  // 关联ID
    private String description;              // 详细描述
}
