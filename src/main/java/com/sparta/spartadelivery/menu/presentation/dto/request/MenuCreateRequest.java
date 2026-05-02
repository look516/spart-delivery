package com.sparta.spartadelivery.menu.presentation.dto.request;

import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MenuCreateRequest(
        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        @NotNull(message = "가게 ID는 필수입니다.")
        UUID storeId,

        @Schema(description = "메뉴 카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440011")
        @NotNull(message = "메뉴 카테고리 ID는 필수입니다.")
        UUID menuCategoryId,

        @Schema(description = "메뉴명", example = "불고기 버거")
        @NotBlank(message = "메뉴명은 필수입니다.")
        @Size(max = 100, message = "메뉴명은 최대 100자까지 입력할 수 있습니다.")
        String name,

        @Schema(description = "가격", example = "5500")
        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @Schema(description = "메뉴 설명", example = "맛있는 불고기 버거입니다.")
        String description,

        @Schema(description = "메뉴 사진", example = "http://image.com")
        String menuPictureUrl,

        //@Schema(description = "숨김 여부", example = "false")
        //boolean isHidden,

        @Schema(description = "AI 메뉴 설명", example = "달콤한 불고기 양념 소고기 패티와 신선한 야채가 어우러진 한국식 햄버거입니다.")
        String aiDescription,

        @Schema(description = "AI 메뉴 프롬프트", example = "불고기 버거 메뉴 설명을 해줘.")
        String aiPrompt


) {
    public MoneyVO toVO() {
        return new MoneyVO(this.price);
    }
}
