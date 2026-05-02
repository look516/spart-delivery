package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
import com.sparta.spartadelivery.menu.exception.MenuErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_menu")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")
    private UUID id;

    // 메뉴 판매 가게 (N : 1)
    @Column(nullable = false)
    private UUID storeId;

    // 메뉴 카테고리 (N : 1)
    @Column(nullable = false)
    private UUID menuCategoryId;

    @Column(length = 100, nullable = false)
    private String name;

    @Embedded
    //@AttributeOverride(name = "price", column = @Column(name = "price", nullable = false))
    private MoneyVO price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String menuPictureUrl;

    @Column // Default false
    private boolean isHidden;

    @Column(columnDefinition = "TEXT")
    private String aiDescription;

    @Column(columnDefinition = "TEXT")
    private String aiPrompt;

    // 생성자 or builder
    public Menu(
            UUID storeId,
            UUID menuCategoryId,
            String name,
            Integer price,
            String description,
            String menuPictureUrl,
            //boolean isHidden, // 추후 제거
            String aiDescription,
            String aiPrompt) {

        this.storeId = storeId;
        this.menuCategoryId = menuCategoryId;
        this.name = name;
        this.price = new MoneyVO(price);
        this.description = description;
        this.menuPictureUrl = menuPictureUrl;
        this.isHidden = false; // 추후 isHidden -> false
        this.aiDescription = aiDescription;
        this.aiPrompt = aiPrompt;
    }


    public void update(
            //UUID storeId,
            UUID menuCategoryId,
            String name,
            Integer price,
            String description,
            String menuPictureUrl,
            //boolean isHidden, // 추후 제거
            String aiDescription,
            String aiPrompt) {

        //this.storeId = storeId;
        this.menuCategoryId = menuCategoryId;
        this.name = name;
        this.price = new MoneyVO(price);
        this.description = description;
        this.menuPictureUrl = menuPictureUrl;
        //this.isHidden = isHidden; // 추후 제거
        this.aiDescription = aiDescription;
        this.aiPrompt = aiPrompt;
    }

    public void hide() {
        if (this.isHidden) {
            throw new AppException(MenuErrorCode.MENU_ALREADY_HIDDEN);
        }
        this.isHidden = true;
    }

    public void show() {
        if (!this.isHidden) {
            throw new AppException(MenuErrorCode.MENU_ALREADY_SHOW);
        }
        this.isHidden = false;
    }
}