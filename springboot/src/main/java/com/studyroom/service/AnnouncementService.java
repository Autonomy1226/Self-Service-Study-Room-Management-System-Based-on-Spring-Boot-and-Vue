package com.studyroom.service;

import com.studyroom.entity.Announcement;
import java.util.List;

public interface AnnouncementService {
    List<Announcement> getActiveAnnouncements();
    List<Announcement> getAllAnnouncements();
    Announcement getAnnouncementById(Long id);
    boolean createAnnouncement(Announcement announcement);
    boolean updateAnnouncement(Announcement announcement);
    boolean deleteAnnouncement(Long id);
}
