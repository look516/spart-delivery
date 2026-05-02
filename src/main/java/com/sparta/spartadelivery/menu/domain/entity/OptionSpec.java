package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_option_spec")
public class OptionSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_spec_id")
    private UUID id;

    // 옵션 그룹 스펙 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_spec_id", nullable = false)
    private OptionGroupSpec optionGroupSpec;

    @Column(length = 100, nullable = false)
    private String name;

    @Embedded
    private MoneyVO price;

    @Column // Default false
    private boolean isDefault;

    public OptionSpec(OptionGroupSpec optionGroupSpec, String name, Integer price, Boolean isDefault) {
        this.optionGroupSpec = optionGroupSpec;
        this.name = name;
        this.price = new MoneyVO(price);
        this.isDefault = isDefault;
    }

    public void update(OptionGroupSpec optionGroupSpec, String name, Integer price, Boolean isDefault) {
        this.optionGroupSpec = optionGroupSpec;
        this.name = name;
        this.price = new MoneyVO(price);
        this.isDefault = isDefault;
    }
}