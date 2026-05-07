package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.service.ThirdPartyPaymentService;
import com.studyroom.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/third-party-payment")
public class ThirdPartyPaymentController {

    @Autowired
    @Qualifier("alipayService")
    private ThirdPartyPaymentService alipayService;

    @Autowired
    @Qualifier("wechatService")
    private ThirdPartyPaymentService wechatService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单
     */
    @PostMapping("/create/{paymentMethod}")
    public Result<Map<String, Object>> createPayment(
            @PathVariable String paymentMethod,
            @RequestParam String orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String subject,
            @RequestParam String description) {
        
        try {
            ThirdPartyPaymentService service = getPaymentService(paymentMethod);
            if (service == null) {
                return Result.error("不支持的支付方式");
            }
            
            Map<String, Object> result = service.createPayment(orderId, amount, subject, description);
            
            if ((Boolean) result.get("success")) {
                return Result.success(result);
            } else {
                return Result.error((String) result.get("error"));
            }
            
        } catch (Exception e) {
            return Result.error("创建支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/query/{paymentMethod}")
    public Result<Map<String, Object>> queryPayment(
            @PathVariable String paymentMethod,
            @RequestParam String orderId) {
        
        try {
            ThirdPartyPaymentService service = getPaymentService(paymentMethod);
            if (service == null) {
                return Result.error("不支持的支付方式");
            }
            
            Map<String, Object> result = service.queryPayment(orderId);
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("查询支付状态失败: " + e.getMessage());
        }
    }

    /**
     * 支付回调
     */
    @PostMapping("/callback/{paymentMethod}")
    public String handleCallback(
            @PathVariable String paymentMethod,
            HttpServletRequest request) {
        
        try {
            ThirdPartyPaymentService service = getPaymentService(paymentMethod);
            if (service == null) {
                return "FAIL";
            }
            
            // 获取回调参数
            Map<String, String> params = extractRequestParams(request);
            
            boolean success = service.handleCallback(params);
            
            if (success) {
                // 更新支付状态
                String orderId = params.get("out_trade_no");
                paymentService.paymentSuccess(orderId, params.get("transaction_id"));
                return "SUCCESS";
            } else {
                return "FAIL";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL";
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund/{paymentMethod}")
    public Result<Map<String, Object>> refund(
            @PathVariable String paymentMethod,
            @RequestParam String orderId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam String reason) {
        
        try {
            ThirdPartyPaymentService service = getPaymentService(paymentMethod);
            if (service == null) {
                return Result.error("不支持的支付方式");
            }
            
            Map<String, Object> result = service.refund(orderId, refundAmount, reason);
            
            if ((Boolean) result.get("success")) {
                // 更新退款状态
                paymentService.refundPayment(Long.parseLong(orderId), reason);
                return Result.success(result);
            } else {
                return Result.error((String) result.get("error"));
            }
            
        } catch (Exception e) {
            return Result.error("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付服务
     */
    private ThirdPartyPaymentService getPaymentService(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "ALIPAY":
                return alipayService;
            case "WECHAT":
                return wechatService;
            default:
                return null;
        }
    }

    /**
     * 提取请求参数
     */
    private Map<String, String> extractRequestParams(HttpServletRequest request) {
        Map<String, String> params = new java.util.HashMap<>();
        
        // 从request中提取所有参数
        java.util.Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }
        
        return params;
    }
}
