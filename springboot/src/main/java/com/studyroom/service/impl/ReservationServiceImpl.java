package com.studyroom.service.impl;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.entity.User;
import com.studyroom.entity.Payment;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import com.studyroom.service.UserService;
import com.studyroom.service.NotificationService;
import com.studyroom.service.BalanceService;
import com.studyroom.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl extends BaseServiceImpl<Reservation, ReservationMapper> implements ReservationService {
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BalanceService balanceService;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Override
    public List<Reservation> getByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Reservation> getBySeatId(Long seatId) {
        return baseMapper.selectBySeatId(seatId);
    }
    
    @Override
    public List<Reservation> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    public List<Reservation> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // 检查用户是否存在
        User user = userService.getById(reservation.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查座位是否存在
        Seat seat = seatService.getById(reservation.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        // 检查时间是否有效
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        if (reservation.getStartTime().isBefore(fiveMinutesAgo)) {
            throw new RuntimeException("开始时间不能早于当前时间的前五分钟");
        }
        if (reservation.getEndTime().isBefore(reservation.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }
        
        // 检查时间冲突
        List<Reservation> existingReservations = getBySeatId(reservation.getSeatId());
        System.out.println("=== 开始冲突检查===");
        System.out.println("新预约 座位=" + reservation.getSeatId() + 
                         ", 开始=" + reservation.getStartTime() + 
                         ", 结束=" + reservation.getEndTime());
        System.out.println("现有预约数量: " + existingReservations.size());
        
        for (Reservation existing : existingReservations) {
            System.out.println("检查现有预约 ID=" + existing.getId() + 
                             ", 状态=" + existing.getStatus() + 
                             ", 开始=" + existing.getStartTime() + 
                             ", 结束=" + existing.getEndTime());
            
            // 检查所有有效状态的预约（PENDING, CONFIRMED, IN_USE）
            boolean isValidStatus = "PENDING".equals(existing.getStatus()) || 
                                  "CONFIRMED".equals(existing.getStatus()) || 
                                  "IN_USE".equals(existing.getStatus());
            
            System.out.println("状态有效 " + isValidStatus);
            
            if (!isValidStatus) {
                System.out.println("跳过无效状态的预约: " + existing.getStatus());
                continue;
            }
            
            // 检查时间是否冲突
            boolean hasConflict = isDateAndTimeOverlap(
                reservation.getStartTime(), 
                reservation.getEndTime(),
                existing.getStartTime(),
                existing.getEndTime()
            );
            
            System.out.println("时间重叠计算:");
            System.out.println("  新预约 " + reservation.getStartTime() + " ~ " + reservation.getEndTime());
            System.out.println("  现预约 " + existing.getStartTime() + " ~ " + existing.getEndTime());
            System.out.println("  end1 < start2: " + reservation.getEndTime().isBefore(existing.getStartTime()));
            System.out.println("  start1 > end2: " + reservation.getStartTime().isAfter(existing.getEndTime()));
            System.out.println("  冲突结果: " + hasConflict);
            
            if (hasConflict) {
                System.out.println("发现冲突，抛出异常");
                throw new RuntimeException("该时间段已被其他用户预约");
            }
        }
        
        System.out.println("=== 冲突检查完成，无冲突===");
        
        // 计算总价（每30分钟为一个单位）
        Duration duration = Duration.between(reservation.getStartTime(), reservation.getEndTime());
        long minutes = duration.toMinutes();
        // 计算30分钟的单位数，向上取整
        long units = (minutes + 29) / 30; // +29是为了向上取整
        BigDecimal totalPrice = seat.getPrice().multiply(BigDecimal.valueOf(units))
            .setScale(2, RoundingMode.HALF_UP);
        reservation.setTotalPrice(totalPrice);
        
        // 设置初始状态
        reservation.setStatus("PENDING");
        
        // 创建预约
        Reservation createdReservation = create(reservation);
        
        // 自动创建支付记录
        Payment payment = new Payment();
        payment.setUserId(reservation.getUserId());
        payment.setReservationId(createdReservation.getId());
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setAmount(totalPrice);
        payment.setPaymentMethod("BALANCE"); // 默认使用余额支付
        payment.setStatus("PENDING");
        payment.setTransactionId(null);
        payment.setPaymentTime(null);
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        
        paymentMapper.insert(payment);
        
        // 如果预约开始时间在当前时间前后5分钟内，立即更新座位状态为不可用
        LocalDateTime fiveMinutesLater = now.plusMinutes(5);
        
        if (reservation.getStartTime().isAfter(now.minusMinutes(5)) && 
            reservation.getStartTime().isBefore(fiveMinutesLater)) {
            // 马上要开始的预约，直接设为不可用
            seatService.updateStatus(reservation.getSeatId(), "UNAVAILABLE");
        } else {
            // 未来的预约，设为不可用
            seatService.updateStatus(reservation.getSeatId(), "UNAVAILABLE");
        }
        
        return createdReservation;
    }
    
    @Override
    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否在允许取消的时间内（预约开始前30分钟以上可以免费取消）
        if (reservation.getStartTime().isBefore(now.plusMinutes(30))) {
            throw new RuntimeException("预约开始前30分钟内不允许取消");
        }
        
        // 查找相关的支付记�?
        List<Payment> payments = paymentMapper.selectByReservationId(reservation.getId());
        
        for (Payment payment : payments) {
            if ("SUCCESS".equals(payment.getStatus())) {
                // 处理退�?
                processRefund(payment, "用户取消预约");
            } else if ("PENDING".equals(payment.getStatus())) {
                // 取消未支付的支付记录
                payment.setStatus("REFUNDED");
                payment.setUpdateTime(now);
                paymentMapper.updateById(payment);
            }
        }
        
        // 发送取消通知
        notificationService.sendReservationCancelled(
            reservation.getUserId(), 
            reservation.getId(), 
            reservation.getSeatId()
        );
        
        // 恢复座位状态为可用
        seatService.updateStatus(reservation.getSeatId(), "AVAILABLE");
        
        // 更新预约状�?
        updateStatus(id, "CANCELLED");
    }
    
    /**
     * 处理退�?
     */
    private void processRefund(Payment payment, String reason) {
        LocalDateTime now = LocalDateTime.now();
        
        // 创建退款记�?
        Payment refundPayment = new Payment();
        refundPayment.setUserId(payment.getUserId());
        refundPayment.setReservationId(payment.getReservationId());
        refundPayment.setPaymentNo("REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        refundPayment.setAmount(payment.getAmount().negate()); // 负数表示退�?
        refundPayment.setPaymentMethod(payment.getPaymentMethod());
        refundPayment.setStatus("SUCCESS");
        refundPayment.setTransactionId("REFUND" + payment.getTransactionId());
        refundPayment.setPaymentTime(now);
        refundPayment.setCreateTime(now);
        refundPayment.setUpdateTime(now);
        
        paymentMapper.insert(refundPayment);
        
        // 更新原支付记录状态为已退�?
        payment.setStatus("REFUNDED");
        payment.setUpdateTime(now);
        paymentMapper.updateById(payment);
        
        // 如果是余额支付，需要退还到用户余额
        if ("BALANCE".equals(payment.getPaymentMethod())) {
            balanceService.addBalance(payment.getUserId(), payment.getAmount());
        }
    }
    
    /**
     * 检查两个时间段是否重叠
     */
    private boolean isDateAndTimeOverlap(
            LocalDateTime start1, 
            LocalDateTime end1, 
            LocalDateTime start2, 
            LocalDateTime end2) {
        // 检查时间重�?
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }
} 
