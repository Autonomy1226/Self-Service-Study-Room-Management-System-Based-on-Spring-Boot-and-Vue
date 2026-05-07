package com.studyroom.task;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 座位状态管理定时任务
 */
@Component
public class SeatStatusTask {
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 每分钟检查一次座位状态
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    @Transactional
    public void updateSeatStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有座位
        List<Seat> allSeats = seatService.getAll();
        
        for (Seat seat : allSeats) {
            updateSingleSeatStatus(seat, now);
        }
    }
    
    /**
     * 更新单个座位的状态
     */
    private void updateSingleSeatStatus(Seat seat, LocalDateTime now) {
        // 查找该座位的有效预约
        List<Reservation> reservations = reservationService.getBySeatId(seat.getId());
        
        boolean hasActiveReservation = false;
        boolean hasUpcomingReservation = false;
        
        for (Reservation reservation : reservations) {
            // 只检查有效状态的预约
            if (!"PENDING".equals(reservation.getStatus()) && !"CONFIRMED".equals(reservation.getStatus()) && !"IN_USE".equals(reservation.getStatus())) {
                continue;
            }
            
            if (isTimeActive(reservation.getStartTime(), reservation.getEndTime(), now)) {
                // 当前时间在预约时间内 -> 有预约
                hasActiveReservation = true;
                
                // 更新预约状态为IN_USE（如果还是CONFIRMED）
                if ("CONFIRMED".equals(reservation.getStatus())) {
                    reservationService.updateStatus(reservation.getId(), "IN_USE");
                    System.out.println("预约状态更新: " + reservation.getId() + " CONFIRMED -> IN_USE");
                }
                break;
            } else if (reservation.getStartTime().isAfter(now)) {
                // 未来时间有预约 -> 有预约
                hasUpcomingReservation = true;
            }
        }
        
        // 更新座位状态
        String newStatus;
        if (hasActiveReservation || hasUpcomingReservation) {
            newStatus = "UNAVAILABLE";
        } else {
            newStatus = "AVAILABLE";
        }
        
        // 只有状态变化时才更新
        if (!newStatus.equals(seat.getStatus())) {
            seatService.updateStatus(seat.getId(), newStatus);
        }
    }
    
    /**
     * 检查时间是否在预约时间段内
     */
    private boolean isTimeActive(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
