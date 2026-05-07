package com.studyroom.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserBalance extends BaseEntity {
    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal totalRecharge;

    public UserBalance() {}

    public UserBalance(Long userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
        this.frozenBalance = BigDecimal.ZERO;
        this.totalRecharge = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }

    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }
}
