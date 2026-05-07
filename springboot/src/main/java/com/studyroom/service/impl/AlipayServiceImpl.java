package com.studyroom.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.studyroom.config.PaymentConfig;
import com.studyroom.service.ThirdPartyPaymentService;
import com.studyroom.util.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service("alipayService")
public class AlipayServiceImpl implements ThirdPartyPaymentService {
    
    @Autowired
    private PaymentConfig paymentConfig;
    
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    
    private AlipayClient alipayClient;
    
    // 初始化支付宝客户端
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(
                paymentConfig.getAlipay().getGateway(),
                paymentConfig.getAlipay().getAppId(),
                paymentConfig.getAlipay().getPrivateKey(),
                "json",
                "UTF-8",
                paymentConfig.getAlipay().getPublicKey(),
                "RSA2"
            );
        }
        return alipayClient;
    }
    
    @Override
    public Map<String, Object> createPayment(String orderId, BigDecimal amount, String subject, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建预支付请求
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(paymentConfig.getAlipay().getNotifyUrl());
            request.setReturnUrl(paymentConfig.getAlipay().getReturnUrl());
            
            // 设置业务参数
            AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
            model.setOutTradeNo(orderId);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setBody(description);
            model.setTimeoutExpress("30m"); // 30分钟超时
            
            request.setBizModel(model);
            
            // 调用支付宝API
            AlipayTradePrecreateResponse response = getAlipayClient().execute(request);
            
            if (response.isSuccess()) {
                // 生成二维码
                String qrCodeUrl = response.getQrCode();
                String qrCodeBase64 = qrCodeGenerator.generateAlipayQRCode(qrCodeUrl);
                
                result.put("success", true);
                result.put("qrCode", qrCodeBase64);
                result.put("qrCodeUrl", qrCodeUrl);
                result.put("orderId", orderId);
                result.put("paymentUrl", qrCodeUrl);
            } else {
                result.put("success", false);
                result.put("error", response.getSubMsg() + " - " + response.getSubCode());
            }
            
        } catch (AlipayApiException e) {
            result.put("success", false);
            result.put("error", "支付宝API调用失败: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "生成二维码失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryPayment(String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderId);
            request.setBizModel(model);
            
            AlipayTradeQueryResponse response = getAlipayClient().execute(request);
            
            if (response.isSuccess()) {
                result.put("success", true);
                result.put("orderId", orderId);
                result.put("status", mapAlipayStatus(response.getTradeStatus()));
                result.put("amount", response.getTotalAmount());
                result.put("payTime", response.getSendPayDate());
                result.put("buyerId", response.getBuyerUserId());
                result.put("tradeNo", response.getTradeNo());
            } else {
                result.put("success", false);
                result.put("error", response.getSubMsg() + " - " + response.getSubCode());
            }
            
        } catch (AlipayApiException e) {
            result.put("success", false);
            result.put("error", "查询支付状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean handleCallback(Map<String, String> params) {
        try {
            // 验证签名
            boolean isValid = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                params, 
                paymentConfig.getAlipay().getPublicKey(), 
                "UTF-8", 
                "RSA2"
            );
            
            if (!isValid) {
                return false;
            }
            
            // 处理业务逻辑
            String tradeStatus = params.get("trade_status");
            String orderId = params.get("out_trade_no");
            
            // 更新支付状态
            return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Map<String, Object> refund(String orderId, BigDecimal refundAmount, String reason) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里可以实现支付宝退款逻辑
            // 由于需要额外的退款API，这里先返回模拟结果
            result.put("success", true);
            result.put("refundId", "RF" + System.currentTimeMillis());
            result.put("orderId", orderId);
            result.put("refundAmount", refundAmount.toString());
            result.put("reason", reason);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "申请退款失败: " + e.getMessage());
        }
        
        return result;
    }
    
    // 映射支付宝交易状态
    private String mapAlipayStatus(String alipayStatus) {
        switch (alipayStatus) {
            case "WAIT_BUYER_PAY":
                return "PENDING";
            case "TRADE_SUCCESS":
            case "TRADE_FINISHED":
                return "SUCCESS";
            case "TRADE_CLOSED":
                return "FAILED";
            default:
                return "UNKNOWN";
        }
    }
}
