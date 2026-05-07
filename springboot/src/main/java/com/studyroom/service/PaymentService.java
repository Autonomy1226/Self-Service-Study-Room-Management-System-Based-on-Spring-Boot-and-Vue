package com.studyroom.service;

import com.studyroom.entity.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    // 创建支付订单
    Payment createPayment(Long userId, Long reservationId, BigDecimal amount, String paymentMethod);
    
    // 支付成功回调
    boolean paymentSuccess(String paymentNo, String transactionId);
    
    // 支付失败
    boolean paymentFailed(String paymentNo, String reason);
    
    // 退款
    boolean refundPayment(Long paymentId, String reason);
    
    // 获取用户支付记录
    List<Payment> getUserPayments(Long userId);
    
    // 余额支付
    boolean balancePayment(Long userId, Long reservationId, BigDecimal amount);
    
    // 确认支付（用于测试）
    boolean confirmPayment(Long paymentId);
}
