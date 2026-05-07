package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Review;
import com.studyroom.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/seat/{seatId}")
    public Result<List<Review>> getSeatReviews(@PathVariable Long seatId) {
        return Result.success(reviewService.getSeatReviews(seatId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Review>> getUserReviews(@PathVariable Long userId) {
        return Result.success(reviewService.getUserReviews(userId));
    }

    @GetMapping("/{id}")
    public Result<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.success(review);
    }

    @PostMapping
    public Result<Review> createReview(@RequestBody Review review) {
        boolean success = reviewService.createReview(review);
        if (success) {
            return Result.success(review);
        }
        return Result.error("创建评价失败");
    }

    @PutMapping("/{id}")
    public Result<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        review.setId(id);
        boolean success = reviewService.updateReview(review);
        if (success) {
            return Result.success(review);
        }
        return Result.error("更新评价失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        boolean success = reviewService.deleteReview(id);
        if (success) {
            return Result.success();
        }
        return Result.error("删除评价失败");
    }

    @GetMapping("/seat/{seatId}/average")
    public Result<Double> getSeatAverageRating(@PathVariable Long seatId) {
        return Result.success(reviewService.getSeatAverageRating(seatId));
    }

    @GetMapping("/seat/{seatId}/stats")
    public Result<Map<Integer, Long>> getSeatRatingStats(@PathVariable Long seatId) {
        return Result.success(reviewService.getSeatRatingStats(seatId));
    }

    @PostMapping("/{id}/helpful")
    public Result<Void> helpfulReview(@PathVariable Long id) {
        boolean success = reviewService.helpfulReview(id);
        if (success) {
            return Result.success();
        }
        return Result.error("点赞失败");
    }
}
