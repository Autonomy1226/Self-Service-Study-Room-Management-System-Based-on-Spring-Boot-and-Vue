package com.studyroom.mapper;

import com.studyroom.entity.Seat;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SeatMapper extends BaseMapper<Seat> {
    // 根据自习室ID查询座位列表
    List<Seat> selectByRoomId(@Param("roomId") Long roomId);
    
    // 根据状态查询座位列表
    List<Seat> selectByStatus(@Param("status") String status);
    
    // 更新座位状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 根据自习室ID和状态查询座位
    List<Seat> selectByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") String status);
    
    // 根据自习室ID删除座位
    int deleteByRoomId(@Param("roomId") Long roomId);
}