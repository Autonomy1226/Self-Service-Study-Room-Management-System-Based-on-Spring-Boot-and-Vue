package com.studyroom.mapper;

import com.studyroom.entity.Review;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ReviewMapper extends BaseMapper<Review> {
    List<Review> selectBySeatId(@Param("seatId") Long seatId);
    List<Review> selectByUserId(@Param("userId") Long userId);
    List<Review> selectApprovedBySeatId(@Param("seatId") Long seatId);
    Double selectAverageRatingBySeatId(@Param("seatId") Long seatId);
}
