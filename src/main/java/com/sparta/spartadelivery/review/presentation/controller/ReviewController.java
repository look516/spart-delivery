package com.sparta.spartadelivery.review.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.review.presentation.dto.*;
import com.sparta.spartadelivery.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@RestController
public class ReviewController implements ReviewApiSpecification {
    private final ReviewService reviewService;

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PostMapping("/{orderId}")
    public ResponseEntity<ApiResponse<ReviewDetailDto>> createReview(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailDto created = reviewService.create(orderId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), created));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailDto>> getViewDetail(@PathVariable UUID reviewId) {
        ReviewDetailDto response = reviewService.viewDetail(reviewId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID reviewId,
                                                    @RequestBody ReviewUpdateRequest request,
                                                    @AuthenticationPrincipal UserPrincipal updatedBy) {
        Long customerId = updatedBy.getId();
        reviewService.update(reviewId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'MASTER')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID reviewId,
                                                    @AuthenticationPrincipal UserPrincipal deletedBy) {
        Long deletedById = deletedBy.getId();
        String deletedByName = deletedBy.getUsername();
        reviewService.delete(reviewId, new ReviewDeletedInfoDto(deletedById, deletedByName));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<ReviewDetailDto>>> search(
            @Valid ReviewSearchCondition condition
    ) {
        Slice<ReviewDetailDto> response = reviewService.search(condition);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
