package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {
    private Long userId;
    private Long reservationId;             // 预约ID
    private String paymentNo;                // 支付单号
    private BigDecimal amount;               // 支付金额
    private String paymentMethod;           // 支付方式: ALIPAY, WECHAT, CREDIT_CARD, BALANCE
    private String paymentType;             // 支付类型: RESERVATION, DEPOSIT, REFUND, PENALTY
    private String status;                  // 状态: PENDING, SUCCESS, FAILED, REFUNDED
    private String transactionId;           // 第三方交易ID
    private LocalDateTime paymentTime;      // 支付时间
    private LocalDateTime refundTime;       // 退款时间
    private BigDecimal refundAmount;         // 退款金额
    private String refundReason;            // 退款原因
    private String currency;                // 货币类型
    private BigDecimal exchangeRate;        // 汇率
    private String description;             // 支付描述
    private String ipAddress;               // 支付IP
    private String userAgent;              // 用户代理
    private String callbackUrl;            // 回调地址
    private String notifyUrl;               // 通知地址
}
