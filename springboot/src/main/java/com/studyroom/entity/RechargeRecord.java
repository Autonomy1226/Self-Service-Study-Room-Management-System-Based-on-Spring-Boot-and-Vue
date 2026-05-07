package com.studyroom.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RechargeRecord extends BaseEntity {
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;

    public RechargeRecord() {}

    public RechargeRecord(Long userId, BigDecimal amount, String paymentMethod) {
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "PENDING";
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
