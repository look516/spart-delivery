package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_category_id")
    private UUID id;

    // 해당 메뉴 카테고리를 가진 가게 (N : 1)
    @Column(nullable = false)
    private UUID storeId;

    @Column(length = 50, nullable = false)
    private String name;

    /*

    // 상위 카테고리
    @Column(nullable = True)
    private UUID parent_id;


     */

    public MenuCategory(UUID storeId, String name) {
        this.storeId = storeId;
        this.name = name;
    }

    public void update(UUID storeId, String name) {
        this.storeId = storeId;
        this.name = name;
    }
}