package com.sparta.spartadelivery.area.domain.entity;

import com.sparta.spartadelivery.store.domain.entity.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "p_area_store",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"area_id", "store_id"})
        }
)
public class AreaStore {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    protected AreaStore() {
    }

    public AreaStore(Area area, Store store) {
        this.area = area;
        this.store = store;
    }
}
