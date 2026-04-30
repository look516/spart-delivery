package com.sparta.spartadelivery.review.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReviewCreateRequest(

        @Schema(description = "가게 id")
        @NotNull(message = "가게 ID는 필수입니다.")
        UUID storeId,

        @Schema(description = "유저 id")
        @NotNull(message = "유저 ID는 필수입니다.")
        Long customerId,

        @Positive
        @Schema(description = "평점")
        int rating,

        @Schema(description = "리뷰 내용")
        String content
) {
}
