package com.studyroom.service.impl;

import com.studyroom.entity.Review;
import com.studyroom.mapper.ReviewMapper;
import com.studyroom.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public List<Review> getSeatReviews(Long seatId) {
        return reviewMapper.selectApprovedBySeatId(seatId);
    }

    @Override
    public List<Review> getUserReviews(Long userId) {
        return reviewMapper.selectByUserId(userId);
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewMapper.selectById(id);
    }

    @Override
    public boolean createReview(Review review) {
        review.setStatus("APPROVED");
        review.setHelpfulCount(0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        return reviewMapper.insert(review) > 0;
    }

    @Override
    public boolean updateReview(Review review) {
        review.setUpdateTime(LocalDateTime.now());
        return reviewMapper.update(review) > 0;
    }

    @Override
    public boolean deleteReview(Long id) {
        return reviewMapper.deleteById(id) > 0;
    }

    @Override
    public Double getSeatAverageRating(Long seatId) {
        Double avg = reviewMapper.selectAverageRatingBySeatId(seatId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public Map<Integer, Long> getSeatRatingStats(Long seatId) {
        List<Review> reviews = reviewMapper.selectApprovedBySeatId(seatId);
        Map<Integer, Long> stats = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            stats.put(i, 0L);
        }
        for (Review review : reviews) {
            stats.put(review.getRating(), stats.get(review.getRating()) + 1);
        }
        return stats;
    }

    @Override
    public boolean helpfulReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review != null) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            review.setUpdateTime(LocalDateTime.now());
            return reviewMapper.update(review) > 0;
        }
        return false;
    }
}
