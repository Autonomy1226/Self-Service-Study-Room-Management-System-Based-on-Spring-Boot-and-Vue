package com.studyroom.service.impl;

import com.studyroom.entity.Payment;
import com.studyroom.entity.Reservation;
import com.studyroom.entity.UserBalance;
import com.studyroom.mapper.PaymentMapper;
import com.studyroom.service.PaymentService;
import com.studyroom.service.BalanceService;
import com.studyroom.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ReservationService reservationService;

    @Override
    @Transactional
    public Payment createPayment(Long userId, Long reservationId, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setReservationId(reservationId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setStatus("PENDING");
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        
        paymentMapper.insert(payment);
        return payment;
    }

    @Override
    @Transactional
    public boolean paymentSuccess(String paymentNo, String transactionId) {
        Payment payment = getByPaymentNo(paymentNo);
        if (payment != null && "PENDING".equals(payment.getStatus())) {
            payment.setStatus("SUCCESS");
            payment.setTransactionId(transactionId);
            payment.setPaymentTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            
            // 更新预约状态为已确认
            if (payment.getReservationId() != null) {
                reservationService.updateStatus(payment.getReservationId(), "CONFIRMED");
            }
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean paymentFailed(String paymentNo, String reason) {
        Payment payment = getByPaymentNo(paymentNo);
        if (payment != null && "PENDING".equals(payment.getStatus())) {
            payment.setStatus("FAILED");
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean refundPayment(Long paymentId, String reason) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment != null && "SUCCESS".equals(payment.getStatus())) {
            payment.setStatus("REFUNDED");
            payment.setRefundTime(LocalDateTime.now());
            payment.setRefundAmount(payment.getAmount());
            payment.setRefundReason(reason);
            payment.setUpdateTime(LocalDateTime.now());
            
            // 退款到余额
            balanceService.addBalance(payment.getUserId(), payment.getAmount());
            
            paymentMapper.updateById(payment);
            return true;
        }
        return false;
    }

    @Override
    public List<Payment> getUserPayments(Long userId) {
        return paymentMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean balancePayment(Long userId, Long reservationId, BigDecimal amount) {
        // 检查余额是否足够
        UserBalance balance = balanceService.getUserBalance(userId);
        if (balance.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }
        
        // 扣除余额
        if (balanceService.deductBalance(userId, amount)) {
            // 创建支付记录
            Payment payment = createPayment(userId, reservationId, amount, "BALANCE");
            payment.setStatus("SUCCESS");
            payment.setPaymentTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            
            // 更新预约状态为已确认
            reservationService.updateStatus(reservationId, "CONFIRMED");
            
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean confirmPayment(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            return false;
        }
        
        // 更新支付状态为成功
        payment.setStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());
        paymentMapper.updateById(payment);
        
        // 更新预约状态为已确认
        reservationService.updateStatus(payment.getReservationId(), "CONFIRMED");
        
        return true;
    }

    private Payment getByPaymentNo(String paymentNo) {
        return paymentMapper.selectByPaymentNo(paymentNo);
    }
}
