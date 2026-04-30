package com.sparta.spartadelivery.review.presentation.dto;

import com.sparta.spartadelivery.global.type.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewSearchCondition(
        @Schema(description = "가게 ID (필수)")
        @NotNull(message = "가게 ID는 필수입니다.")
        UUID storeId,

        @Schema(description = "조회할 평점 (선택)", example = "5")
        @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점 이하이어야 합니다.")
        Integer rating,

        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
        int page,

        @Schema(description = "페이지당 데이터 개수", example = "10", defaultValue = "10")
        int size,

        @Schema(description = "정렬 기준 (기본값: createdAt)", example = "createdAt")
        String sortBy,

        @Schema(description = "정렬 방향 (기본값: DESC)", example = "DESC", allowableValues = {"ASC", "DESC"})
        SortOrder sort
) {
}