package com.studyroom.service.impl;

import com.studyroom.entity.UserBalance;
import com.studyroom.entity.RechargeRecord;
import com.studyroom.mapper.UserBalanceMapper;
import com.studyroom.mapper.RechargeRecordMapper;
import com.studyroom.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Override
    public UserBalance getUserBalance(Long userId) {
        UserBalance balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null) {
            // 如果没有余额记录，创建一个
            balance = new UserBalance(userId);
            balance.setCreateTime(LocalDateTime.now());
            balance.setUpdateTime(LocalDateTime.now());
            userBalanceMapper.insert(balance);
        }
        return balance;
    }

    @Override
    @Transactional
    public RechargeRecord recharge(Long userId, BigDecimal amount, String paymentMethod) {
        // 创建充值记录
        RechargeRecord record = new RechargeRecord(userId, amount, paymentMethod);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(record);
        
        // 模拟支付成功（实际应该调用第三方支付接口）
        record.setPaymentStatus("SUCCESS");
        record.setTransactionId("TXN" + System.currentTimeMillis());
        record.setUpdateTime(LocalDateTime.now());
        rechargeRecordMapper.updateById(record);
        
        // 增加用户余额
        addBalance(userId, amount);
        
        return record;
    }

    @Override
    @Transactional
    public boolean deductBalance(Long userId, BigDecimal amount) {
        UserBalance balance = getUserBalance(userId);
        if (balance.getBalance().compareTo(amount) < 0) {
            return false;
        }
        
        balance.setBalance(balance.getBalance().subtract(amount));
        balance.setUpdateTime(LocalDateTime.now());
        userBalanceMapper.updateById(balance);
        return true;
    }

    @Override
    @Transactional
    public boolean freezeBalance(Long userId, BigDecimal amount) {
        UserBalance balance = getUserBalance(userId);
        if (balance.getBalance().compareTo(amount) < 0) {
            return false;
        }
        
        balance.setBalance(balance.getBalance().subtract(amount));
        balance.setFrozenBalance(balance.getFrozenBalance().add(amount));
        balance.setUpdateTime(LocalDateTime.now());
        userBalanceMapper.updateById(balance);
        return true;
    }

    @Override
    @Transactional
    public boolean unfreezeBalance(Long userId, BigDecimal amount) {
        UserBalance balance = getUserBalance(userId);
        if (balance.getFrozenBalance().compareTo(amount) < 0) {
            return false;
        }
        
        balance.setBalance(balance.getBalance().add(amount));
        balance.setFrozenBalance(balance.getFrozenBalance().subtract(amount));
        balance.setUpdateTime(LocalDateTime.now());
        userBalanceMapper.updateById(balance);
        return true;
    }

    @Override
    public List<RechargeRecord> getRechargeRecords(Long userId) {
        return rechargeRecordMapper.selectByUserId(userId);
    }

    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {
        UserBalance balance = getUserBalance(userId);
        balance.setBalance(balance.getBalance().add(amount));
        balance.setTotalRecharge(balance.getTotalRecharge().add(amount));
        balance.setUpdateTime(LocalDateTime.now());
        userBalanceMapper.updateById(balance);
    }
}
