package com.studyroom.mapper;

import com.studyroom.entity.Notification;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface NotificationMapper extends BaseMapper<Notification> {
    List<Notification> selectByUserId(@Param("userId") Long userId);
    List<Notification> selectUnreadByUserId(@Param("userId") Long userId);
    int countUnreadByUserId(@Param("userId") Long userId);
    int markAllAsRead(@Param("userId") Long userId);
}
