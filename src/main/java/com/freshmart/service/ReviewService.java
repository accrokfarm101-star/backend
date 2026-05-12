package com.freshmart.service;

import com.freshmart.model.dto.request.ReviewRequest;
import com.freshmart.model.dto.response.ReviewResponse;
import com.freshmart.model.dto.response.ProductReviewStatsResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(Long productId, Long userId, ReviewRequest reviewRequest);

    ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest reviewRequest);

    ReviewResponse getReviewById(Long reviewId);

    List<ReviewResponse> getProductReviews(Long productId);

    List<ReviewResponse> getUserReviews(Long userId);

    ProductReviewStatsResponse getProductReviewStats(Long productId);

    void deleteReview(Long reviewId, Long userId);

    void deleteReviewByAdmin(Long reviewId);
}
