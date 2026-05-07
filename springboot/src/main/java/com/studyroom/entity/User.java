package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;
    
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;
    
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|USER)$", message = "角色只能是ADMIN或USER")
    private String role;
    
    // 新增字段
    private String avatar;                  // 头像URL
    private String status;                  // 用户状态: ACTIVE, INACTIVE, BANNED
    private Integer creditScore;            // 信用分数
    private String memberLevel;             // 会员等级
    private LocalDateTime memberExpireTime; // 会员到期时间
    private LocalDateTime lastLoginTime;    // 最后登录时间
    private String lastLoginIp;             // 最后登录IP
    private Boolean emailVerified;          // 邮箱是否验证
    private Boolean phoneVerified;          // 手机是否验证
    private LocalDateTime registerTime;     // 注册时间
    private String invitationCode;          // 邀请码
    private Long inviterId;                 // 邀请人ID
}