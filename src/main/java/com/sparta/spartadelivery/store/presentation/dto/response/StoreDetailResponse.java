package com.sparta.spartadelivery.store.presentation.dto.response;

import com.sparta.spartadelivery.store.domain.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreDetailResponse(
        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440012")
        UUID id,

        @Schema(description = "가게 소유자 ID", example = "1")
        Long ownerId,

        @Schema(description = "가게 카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        UUID storeCategoryId,

        @Schema(description = "지역 ID", example = "550e8400-e29b-41d4-a716-446655440011")
        UUID areaId,

        @Schema(description = "가게명", example = "스파르타 분식")
        String name,

        @Schema(description = "가게 주소", example = "서울특별시 강남구 테헤란로 123")
        String address,

        @Schema(description = "가게 연락처", example = "02-1234-5678")
        String phone,

        @Schema(description = "평균 평점", example = "0.0")
        BigDecimal averageRating,

        @Schema(description = "숨김 여부", example = "false")
        boolean hidden,

        @Schema(description = "가게 생성 일시", example = "2026-04-24T12:00:00")
        LocalDateTime createdAt
) {

    public static StoreDetailResponse from(Store store) {
        return new StoreDetailResponse(
                store.getId(),
                store.getOwner().getId(),
                store.getStoreCategory().getId(),
                store.getArea().getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                getAverageRating(store.getRatingSum(), store.getRatingCount()),
                store.isHidden(),
                store.getCreatedAt()
        );
    }

    private static BigDecimal getAverageRating(int ratingSum, int ratingCount) {
        // 1. 리뷰가 하나도 없는 경우(분모가 0) 처리
        if (ratingCount == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        // 2. NaN이나 Infinity 발생을 방지하기 위해 정수 연산 후 변환하거나,
        // 값을 double로 만든 후 유효성 체크를 거칩니다.
        double avg = (double) ratingSum / ratingCount;

        return BigDecimal.valueOf(avg)
                .setScale(1, RoundingMode.HALF_UP);
    }
}
