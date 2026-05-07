package com.studyroom.mapper;

import com.studyroom.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationMapper extends BaseMapper<Reservation> {
    // 根据用户ID查询预约列表
    List<Reservation> selectByUserId(@Param("userId") Long userId);
    
    // 根据座位ID查询预约列表
    List<Reservation> selectBySeatId(@Param("seatId") Long seatId);
    
    // 根据状态查询预约列表
    List<Reservation> selectByStatus(@Param("status") String status);
    
    // 查询指定时间段的预约
    List<Reservation> selectByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 更新预约状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 根据自习室ID统计预约数量
    int countByRoomId(@Param("roomId") Long roomId, 
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);
}