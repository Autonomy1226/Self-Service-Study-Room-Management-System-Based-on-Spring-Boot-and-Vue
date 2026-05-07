package com.studyroom.service;

import com.studyroom.entity.UserBalance;
import com.studyroom.entity.RechargeRecord;
import java.math.BigDecimal;
import java.util.List;

public interface BalanceService {
    // 获取用户余额
    UserBalance getUserBalance(Long userId);
    
    // 充值
    RechargeRecord recharge(Long userId, BigDecimal amount, String paymentMethod);
    
    // 余额支付
    boolean deductBalance(Long userId, BigDecimal amount);
    
    // 冻结余额
    boolean freezeBalance(Long userId, BigDecimal amount);
    
    // 解冻余额
    boolean unfreezeBalance(Long userId, BigDecimal amount);
    
    // 获取充值记录
    List<RechargeRecord> getRechargeRecords(Long userId);
    
    // 添加余额（内部方法）
    void addBalance(Long userId, BigDecimal amount);
}
