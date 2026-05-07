package com.studyroom.service.impl;

import com.studyroom.entity.Notification;
import com.studyroom.mapper.NotificationMapper;
import com.studyroom.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public boolean createNotification(Notification notification) {
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        return notificationMapper.insert(notification) > 0;
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setIsRead(true);
            notification.setUpdateTime(LocalDateTime.now());
            return notificationMapper.update(notification) > 0;
        }
        return false;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId) > 0;
    }

    @Override
    public boolean deleteNotification(Long id) {
        return notificationMapper.deleteById(id) > 0;
    }

    @Override
    public void sendReservationReminder(Long userId, Long reservationId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("RESERVATION_REMINDER");
        notification.setTitle("预约即将开始");
        notification.setContent(message);
        notification.setRelatedId(reservationId);
        notification.setRelatedType("RESERVATION");
        createNotification(notification);
    }

    @Override
    public void sendReservationExpired(Long userId, Long reservationId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("RESERVATION_EXPIRED");
        notification.setTitle("预约已结束");
        notification.setContent("您的预约时间已结束，如有需要请重新预约座位。");
        notification.setRelatedId(reservationId);
        notification.setRelatedType("RESERVATION");
        createNotification(notification);
    }

    @Override
    public void sendReservationCancelled(Long userId, Long reservationId, Long seatId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("RESERVATION_CANCELLED");
        notification.setTitle("预约已取消");
        notification.setContent("您的预约已成功取消。如需重新预约，请选择合适的时间段。");
        notification.setRelatedId(reservationId);
        notification.setRelatedType("RESERVATION");
        createNotification(notification);
    }
}
