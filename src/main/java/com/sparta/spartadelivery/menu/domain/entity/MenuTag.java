package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.menu.domain.vo.MenuTagVO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_tag_id")
    private UUID id;

    @Embedded
    private MenuTagVO menuTagInfo;

    private MenuTag(MenuTagVO vo) {
        this.menuTagInfo = vo;
    }

    public static MenuTag from(MenuTagVO vo) {
        return new MenuTag(vo);
    }

    public void update(MenuTagVO vo) {
        this.menuTagInfo = vo;
    }
}
