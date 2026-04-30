package com.sparta.spartadelivery.menu.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record MenuUpdateRequest(
        @Schema(description = "메뉴 이름", example = "매콤 치즈 떡볶이")
        String name,

        @Schema(description = "가격", example = "15000")
        Integer price,

        @Schema(description = "메뉴 설명", example = "특제 소스로 만든 매콤한 떡볶이입니다.")
        String description,

        @Schema(description = "메뉴 이미지 URL")
        String menuPictureUrl,

        @Schema(description = "숨김 여부")
        boolean isHidden,

        @Schema(description = "메뉴 카테고리 ID")
        UUID menuCategoryId,

        @Schema(description = "AI 설명")
        String aiDescription,

        @Schema(description = "AI 프롬프트")
        String aiPrompt
) {
}