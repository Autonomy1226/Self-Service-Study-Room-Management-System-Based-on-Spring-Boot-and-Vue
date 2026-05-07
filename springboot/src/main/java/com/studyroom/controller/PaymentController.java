package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Payment;
import com.studyroom.entity.UserBalance;
import com.studyroom.entity.RechargeRecord;
import com.studyroom.service.PaymentService;
import com.studyroom.service.BalanceService;
import com.studyroom.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ReservationService reservationService;

    // 获取用户余额
    @GetMapping("/balance/{userId}")
    public Result<UserBalance> getUserBalance(@PathVariable Long userId) {
        UserBalance balance = balanceService.getUserBalance(userId);
        return Result.success(balance);
    }

    // 充值
    @PostMapping("/recharge")
    public Result<RechargeRecord> recharge(@RequestBody RechargeRecord rechargeRecord) {
        RechargeRecord record = balanceService.recharge(
            rechargeRecord.getUserId(), 
            rechargeRecord.getAmount(), 
            rechargeRecord.getPaymentMethod()
        );
        return Result.success(record);
    }

    // 获取用户支付记录
    @GetMapping("/records/{userId}")
    public Result<List<Payment>> getUserPayments(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        return Result.success(payments);
    }

    // 获取用户充值记录
    @GetMapping("/recharge-records/{userId}")
    public Result<List<RechargeRecord>> getRechargeRecords(@PathVariable Long userId) {
        List<RechargeRecord> records = balanceService.getRechargeRecords(userId);
        return Result.success(records);
    }

    // 创建支付订单
    @PostMapping("/create")
    public Result<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(
            payment.getUserId(),
            payment.getReservationId(),
            payment.getAmount(),
            payment.getPaymentMethod()
        );
        return Result.success(createdPayment);
    }

    // 余额支付
    @PostMapping("/balance-pay")
    public Result<Boolean> balancePayment(@RequestBody Payment payment) {
        boolean success = paymentService.balancePayment(
            payment.getUserId(),
            payment.getReservationId(),
            payment.getAmount()
        );
        return Result.success(success);
    }

    // 确认支付（用于测试）
    @PostMapping("/confirm-payment/{paymentId}")
    public Result<Boolean> confirmPayment(@PathVariable Long paymentId) {
        boolean success = paymentService.confirmPayment(paymentId);
        return Result.success(success);
    }
}
