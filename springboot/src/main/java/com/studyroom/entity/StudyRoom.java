package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudyRoom extends BaseEntity {
    @NotBlank(message = "自习室名称不能为空")
    @Size(max = 50, message = "自习室名称长度不能超过50")
    @JsonProperty("name")
    @JsonAlias({"roomName"})
    private String roomName;
    
    @NotNull(message = "容纳人数不能为空")
    @Min(value = 1, message = "容纳人数必须大于0")
    @Max(value = 1000, message = "容纳人数不能超过1000")
    private Integer capacity;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(OPEN|CLOSED|MAINTENANCE)$", message = "状态只能是OPEN、CLOSED或MAINTENANCE")
    private String status;
    
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    @Size(max = 255, message = "平面图地址长度不能超过255")
    private String floorPlanUrl;
} 