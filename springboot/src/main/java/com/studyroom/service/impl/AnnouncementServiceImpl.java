package com.studyroom.service.impl;

import com.studyroom.entity.Announcement;
import com.studyroom.mapper.AnnouncementMapper;
import com.studyroom.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> getActiveAnnouncements() {
        return announcementMapper.selectAll().stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .sorted(Comparator.comparing(Announcement::getPriority).reversed()
                        .thenComparing(Announcement::getCreateTime, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.selectAll();
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public boolean createAnnouncement(Announcement announcement) {
        return announcementMapper.insert(announcement) > 0;
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        return announcementMapper.update(announcement) > 0;
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        return announcementMapper.deleteById(id) > 0;
    }
}
