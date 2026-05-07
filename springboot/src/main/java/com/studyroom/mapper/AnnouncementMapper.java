package com.studyroom.mapper;

import com.studyroom.entity.Announcement;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AnnouncementMapper extends BaseMapper<Announcement> {
    List<Announcement> selectByStatus(@Param("status") String status);
    List<Announcement> selectByType(@Param("type") String type);
}
