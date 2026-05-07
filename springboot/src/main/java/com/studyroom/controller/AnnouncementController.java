package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Announcement;
import com.studyroom.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/active")
    public Result<List<Announcement>> getActiveAnnouncements() {
        return Result.success(announcementService.getActiveAnnouncements());
    }

    @GetMapping
    public Result<List<Announcement>> getAllAnnouncements() {
        return Result.success(announcementService.getAllAnnouncements());
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        boolean success = announcementService.createAnnouncement(announcement);
        if (success) {
            return Result.success(announcement);
        }
        return Result.error("创建公告失败");
    }

    @PutMapping("/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcement.setUpdateTime(LocalDateTime.now());
        boolean success = announcementService.updateAnnouncement(announcement);
        if (success) {
            return Result.success(announcement);
        }
        return Result.error("更新公告失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            return Result.success();
        }
        return Result.error("删除公告失败");
    }
}
