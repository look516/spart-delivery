package com.sparta.spartadelivery.review.domain.repository;

import com.sparta.spartadelivery.review.domain.entity.Review;
import com.sparta.spartadelivery.review.presentation.dto.ReviewSearchCondition;
import org.springframework.data.domain.Slice;

public interface ReviewSearchRepository {
    Slice<Review> search(ReviewSearchCondition condition);
}