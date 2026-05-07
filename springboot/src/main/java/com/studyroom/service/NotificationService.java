package com.studyroom.service;

import com.studyroom.entity.Notification;
import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications(Long userId);
    List<Notification> getUnreadNotifications(Long userId);
    int getUnreadCount(Long userId);
    boolean createNotification(Notification notification);
    boolean markAsRead(Long notificationId);
    boolean markAllAsRead(Long userId);
    boolean deleteNotification(Long id);
    // 发送预约提醒通知
    void sendReservationReminder(Long userId, Long reservationId, String message);
    // 发送预约过期通知
    void sendReservationExpired(Long userId, Long reservationId);
    
    void sendReservationCancelled(Long userId, Long reservationId, Long seatId);
}
