package com.sparta.spartadelivery.store.domain.entity;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_category_id", nullable = false)
    private StoreCategory storeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

//    @Column(name = "average_rating", precision = 2, scale = 1, nullable = false)
//    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_sum")
    private int ratingSum;

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @Builder
    private Store(
            UserEntity owner,
            StoreCategory storeCategory,
            Area area,
            String name,
            String address,
            String phone
    ) {
        this.owner = owner;
        this.storeCategory = storeCategory;
        this.area = area;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void update(StoreCategory storeCategory, Area area, String name, String address, String phone) {
        this.storeCategory = storeCategory;
        this.area = area;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

//    public void updateAverageRating(BigDecimal averageRating) {
//        this.averageRating = averageRating;
//    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }
}
