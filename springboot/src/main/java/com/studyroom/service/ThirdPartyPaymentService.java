package com.studyroom.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ThirdPartyPaymentService {
    
    /**
     * 创建支付订单
     * @param orderId 订单号
     * @param amount 支付金额
     * @param subject 支付标题
     * @param description 支付描述
     * @return 支付信息（包含支付URL等）
     */
    Map<String, Object> createPayment(String orderId, BigDecimal amount, String subject, String description);
    
    /**
     * 查询支付状态
     * @param orderId 订单号
     * @return 支付状态信息
     */
    Map<String, Object> queryPayment(String orderId);
    
    /**
     * 处理支付回调
     * @param params 回调参数
     * @return 处理结果
     */
    boolean handleCallback(Map<String, String> params);
    
    /**
     * 申请退款
     * @param orderId 原订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    Map<String, Object> refund(String orderId, BigDecimal refundAmount, String reason);
}
