package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
}
