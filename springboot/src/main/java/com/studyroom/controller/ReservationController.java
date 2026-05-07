package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Reservation;
import com.studyroom.service.ReservationService;
import com.studyroom.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public Result<Reservation> create(@RequestBody Reservation reservation) {
        try {
            return Result.success(reservationService.createReservation(reservation));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Reservation> getById(@PathVariable Long id) {
        return Result.success(reservationService.getById(id));
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<Reservation>> getByUserId(@PathVariable Long userId) {
        return Result.success(reservationService.getByUserId(userId));
    }
    
    @GetMapping("/seat/{seatId}")
    public Result<List<Reservation>> getBySeatId(@PathVariable Long seatId) {
        return Result.success(reservationService.getBySeatId(seatId));
    }
    
    @GetMapping("/status/{status}")
    public Result<List<Reservation>> getByStatus(@PathVariable String status) {
        return Result.success(reservationService.getByStatus(status));
    }
    
    @GetMapping("/time-range")
    public Result<List<Reservation>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(reservationService.getByTimeRange(startTime, endTime));
    }
    
    @GetMapping
    public Result<List<Reservation>> getAll() {
        return Result.success(reservationService.getAll());
    }
    
    @PutMapping("/{id}")
    public Result<Reservation> update(@PathVariable Long id, @RequestBody Reservation reservation) {
        reservation.setId(id);
        return Result.success(reservationService.update(reservation));
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        reservationService.updateStatus(id, status);
        return Result.success();
    }
    
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return Result.success();
    }
} 