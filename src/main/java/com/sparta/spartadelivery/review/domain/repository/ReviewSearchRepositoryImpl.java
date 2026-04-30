package com.sparta.spartadelivery.review.domain.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.spartadelivery.global.type.SortOrder;
import com.sparta.spartadelivery.review.domain.entity.Review;
import com.sparta.spartadelivery.review.presentation.dto.ReviewSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.spartadelivery.review.domain.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewSearchRepositoryImpl implements ReviewSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Review> search(ReviewSearchCondition condition) {
        int pageSize = condition.size();

        List<Review> content = queryFactory
                .selectFrom(review)
                .where(
                        review.store.id.eq(condition.storeId()),
                        ratingEq(condition.rating())
                )
                // 정렬 기준 문자열과 방향 Enum을 함께 전달
                .orderBy(getSort(condition.sortBy(), condition.sort()))
                .offset((long) condition.page() * pageSize)
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageSize) {
            content.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl<>(content, PageRequest.of(condition.page(), pageSize), hasNext);
    }

    private BooleanExpression ratingEq(Integer rating) {
        return rating != null ? review.rating.eq(rating) : null;
    }

    /**
     * 정렬 조건 처리
     *
     * @param sortBy 정렬 기준 (기본값: "createdAt")
     * @param order  정렬 방향 (기본값: DESC)
     */
    private OrderSpecifier<?> getSort(String sortBy, SortOrder order) {
        // 1. 방향 기본값 설정: ASC가 아니면 무조건 DESC
        boolean isAsc = (order == SortOrder.ASC);

        // 2. sortBy가 null이거나 빈 값이면 기본값 "createdAt" 적용
        if (sortBy == null || sortBy.isBlank()) {
            return isAsc ? review.createdAt.asc() : review.createdAt.desc();
        }

        // 3. 필드에 따른 분기 처리
        return switch (sortBy) {
            case "createdAt" -> isAsc ? review.createdAt.asc() : review.createdAt.desc();
            default -> isAsc ? review.createdAt.asc() : review.createdAt.desc();
        };
    }
}