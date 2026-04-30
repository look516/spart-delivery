package com.sparta.spartadelivery.menu.presentation.dto.response;

import com.sparta.spartadelivery.menu.domain.entity.Menu;

import java.util.UUID;

public record MenuUpdateResponse(
        UUID menuId,
        String name,
        Integer price,
        String description,
        String menuPictureUrl,
        boolean isHidden,
        UUID menuCategoryId,
        String aiDescription,
        String aiPrompt
) {

    public static MenuUpdateResponse from(Menu menu) {
        return new MenuUpdateResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice().getPrice(),
                menu.getDescription(),
                menu.getMenuPictureUrl(),
                menu.isHidden(),
                menu.getMenuCategoryId(),
                menu.getAiDescription(),
                menu.getAiPrompt()
        );
    }
}
