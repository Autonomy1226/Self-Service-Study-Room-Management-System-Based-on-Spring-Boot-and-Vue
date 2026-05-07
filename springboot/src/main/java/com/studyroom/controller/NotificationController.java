package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Notification;
import com.studyroom.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public Result<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return Result.success(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public Result<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        return Result.success(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public Result<Integer> getUnreadCount(@PathVariable Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PostMapping
    public Result<Notification> createNotification(@RequestBody Notification notification) {
        boolean success = notificationService.createNotification(notification);
        if (success) {
            return Result.success(notification);
        }
        return Result.error("创建通知失败");
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        if (success) {
            return Result.success();
        }
        return Result.error("标记已读失败");
    }

    @PutMapping("/user/{userId}/read-all")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        boolean success = notificationService.markAllAsRead(userId);
        if (success) {
            return Result.success();
        }
        return Result.error("标记全部已读失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.deleteNotification(id);
        if (success) {
            return Result.success();
        }
        return Result.error("删除通知失败");
    }
}
