package com.studyroom.service.impl;

import com.studyroom.config.PaymentConfig;
import com.studyroom.service.ThirdPartyPaymentService;
import com.studyroom.util.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("wechatService")
public class WechatServiceImpl implements ThirdPartyPaymentService {
    
    @Autowired
    private PaymentConfig paymentConfig;
    
    @Autowired
    private QRCodeGenerator qrCodeGenerator;
    
    @Override
    public Map<String, Object> createPayment(String orderId, BigDecimal amount, String subject, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 模拟微信支付统一下单
            String codeUrl = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            
            // 生成二维码
            String qrCodeBase64 = qrCodeGenerator.generateWechatQRCode(codeUrl);
            
            result.put("success", true);
            result.put("qrCode", qrCodeBase64);
            result.put("codeUrl", codeUrl);
            result.put("orderId", orderId);
            result.put("paymentUrl", codeUrl);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "微信支付创建失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryPayment(String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 模拟查询支付状态
            result.put("success", true);
            result.put("orderId", orderId);
            result.put("status", "SUCCESS"); // PENDING, SUCCESS, FAILED
            result.put("amount", "10.00");
            result.put("payTime", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "查询支付状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean handleCallback(Map<String, String> params) {
        try {
            // 模拟验证签名
            boolean isValid = verifySign(params);
            if (!isValid) {
                return false;
            }
            
            // 处理业务逻辑
            String orderId = params.get("out_trade_no");
            String resultCode = params.get("result_code");
            
            // 更新支付状态
            return "SUCCESS".equals(resultCode);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Map<String, Object> refund(String orderId, BigDecimal refundAmount, String reason) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 模拟申请退款
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
    
    // 辅助方法
    private boolean verifySign(Map<String, String> params) {
        // 模拟验证MD5签名，这里简化处理
        return true;
    }
}
