package com.studyroom.service;

import com.studyroom.entity.Review;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    List<Review> getSeatReviews(Long seatId);
    List<Review> getUserReviews(Long userId);
    Review getReviewById(Long id);
    boolean createReview(Review review);
    boolean updateReview(Review review);
    boolean deleteReview(Long id);
    // 获取座位平均评分
    Double getSeatAverageRating(Long seatId);
    // 获取座位评分统计
    Map<Integer, Long> getSeatRatingStats(Long seatId);
    // 点赞评价
    boolean helpfulReview(Long reviewId);
}
