package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Announcement extends BaseEntity {
    private String title;
    private String content;
    private String type; // GENERAL-普通, RULE-规则, MAINTENANCE-维护, URGENT-紧急
    private Integer priority; // 优先级: 0-10
    private String status; // ACTIVE-有效, INACTIVE-失效
}
