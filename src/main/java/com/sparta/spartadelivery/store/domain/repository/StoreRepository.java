package com.sparta.spartadelivery.store.domain.repository;

import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("""
            select s
            from Store s
            where s.deletedAt is null
              and s.isHidden = false
            """)
    Page<Store> findAllPublicStores(Pageable pageable);

    Page<Store> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Store> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Store> findByIdAndDeletedAtIsNullAndIsHiddenFalse(UUID id);

    Optional<Store> findByOwner(UserEntity user);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Store s SET s.ratingSum = s.ratingSum + :delta, " +
            "s.ratingCount = s.ratingCount + :countDelta " +
            "WHERE s.id = :id")
    int updateStoreRating(@Param("id") UUID id,
                          @Param("delta") int delta,
                          @Param("countDelta") int countDelta);
}
