package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.menu.domain.vo.SelectionRangeVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_option_group_spec")
public class OptionGroupSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_group_spec_id")
    private UUID id;

    // 메뉴 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(length = 100, nullable = false)
    private String name;

    @Embedded
    private SelectionRangeVO selectionRange;

    public OptionGroupSpec(Menu menu, String name, Integer minSelect, Integer maxSelect) {
        this.menu = menu;
        this.name = name;
        this.selectionRange = new SelectionRangeVO(minSelect, maxSelect);
    }

    public void update(Menu menu, String name, Integer minSelect, Integer maxSelect) {
        this.menu = menu;
        this.name = name;
        this.selectionRange = new SelectionRangeVO(minSelect, maxSelect);
    }
}