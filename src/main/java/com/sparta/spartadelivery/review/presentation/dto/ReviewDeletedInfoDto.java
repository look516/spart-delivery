package com.sparta.spartadelivery.review.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReviewDeletedInfoDto(
        @Schema(description = "리뷰 삭제자의 userid")
        @NotNull(message = "리뷰 삭제자의 userid는 필수입니다.")
        Long loginId,

        @Schema(description = "리뷰 삭제자")
        String userName
) {
}
