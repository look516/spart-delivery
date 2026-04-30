package com.sparta.spartadelivery.review.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.review.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@Tag(name = "Review", description = "리뷰 관련 API")
public interface ReviewApiSpecification {

    @Operation(summary = "리뷰 작성", description = "배달 완료된 주문에 대해 리뷰를 작성합니다. (CUSTOMER 권한 필요)")
    ResponseEntity<ApiResponse<ReviewDetailDto>> createReview(
            @Parameter(description = "주문 ID", required = true) UUID orderId,
            @Valid ReviewCreateRequest request
    );

    @Operation(summary = "리뷰 단건 상세 조회", description = "특정 리뷰의 상세 내용을 조회합니다.")
    ResponseEntity<ApiResponse<ReviewDetailDto>> getViewDetail(
            @Parameter(description = "리뷰 ID", required = true) UUID reviewId
    );

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰의 별점 및 내용을 수정합니다. (작성자 본인만 가능)")
    ResponseEntity<ApiResponse<Void>> update(
            @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
            ReviewUpdateRequest request,
            UserPrincipal updatedBy
    );

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제(Soft Delete)합니다. (작성자 또는 관리자 권한 필요)")
    ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
            UserPrincipal deletedBy
    );

    @Operation(summary = "리뷰 목록 검색/페이징", description = "가게별 리뷰 목록을 검색합니다. (Slice 기반 페이징)")
    ResponseEntity<ApiResponse<Slice<ReviewDetailDto>>> search(
            @Valid @ModelAttribute ReviewSearchCondition condition
    );
}