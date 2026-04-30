package com.sparta.spartadelivery.review.service;

import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.review.domain.entity.Review;
import com.sparta.spartadelivery.review.domain.repository.ReviewRepository;
import com.sparta.spartadelivery.review.domain.repository.ReviewSearchRepository;
import com.sparta.spartadelivery.review.presentation.dto.*;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewSearchRepository reviewSearchRepository;

    @Transactional
    public ReviewDetailDto create(UUID orderId, ReviewCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new EntityNotFoundException("가게를 찾을 수 없습니다. ID: " + request.storeId()));
        UserEntity customer = userRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. ID: " + request.customerId()));

        Review saved = reviewRepository.save(
                new Review(order, store, customer, request.rating(), request.content())
        );
        return ReviewDetailDto.from(saved);
    }

    public ReviewDetailDto viewDetail(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        return ReviewDetailDto.from(review);
    }

    @Transactional
    public void update(UUID reviewId, Long customerId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        review.update(customerId, request.rating(), request.content());
    }

    @Transactional
    public void delete(UUID reviewId, ReviewDeletedInfoDto deletedInfo) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        review.delete(deletedInfo.loginId(), deletedInfo.userName());
    }

    public Slice<ReviewDetailDto> search(ReviewSearchCondition condition) {
        Slice<Review> reviews = reviewSearchRepository.search(condition);
        return ReviewDetailDto.from(reviews);
    }

}
