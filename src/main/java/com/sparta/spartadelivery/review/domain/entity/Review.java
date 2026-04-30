package com.sparta.spartadelivery.review.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.review.domain.event.ReviewCreatedEvent;
import com.sparta.spartadelivery.review.domain.event.ReviewDeletedEvent;
import com.sparta.spartadelivery.review.domain.event.ReviewUpdatedEvent;
import com.sparta.spartadelivery.review.exception.ReviewErrorCode;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.util.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity customer;
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    private int rating;

    private String content;

    //region [도메인 이벤트 관리를 위한 필드]
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    @DomainEvents
    public Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    private void registerEvent(Object event) {
        this.domainEvents.add(event);
    }
    //endregion

    public Review(UUID id, Order order, Store store, UserEntity customer, int rating, String content) {
        this.id = id;
        this.order = order;
        this.store = store;
        this.customer = customer;
        this.rating = rating;
        this.content = content;
        validate();

        registerEvent(new ReviewCreatedEvent(this.store.getId(), this.rating));
    }

    public Review(Order order, Store store, UserEntity customer, int rating, String content) {
        this(UUID.randomUUID(), order, store, customer, rating, content);
    }

    private void validate() {
        validateOrder(this.order);
        validateRating(this.rating);
    }

    public void update(Long loginUserId, int rating, String content) {
        int oldRating = this.rating;

        verifyCustomer(loginUserId);
        validateRating(rating);

        this.rating = rating;
        this.content = content;

        registerEvent(new ReviewUpdatedEvent(this.store.getId(), oldRating, rating));
    }

    public void delete(Long loginUserId, String userName) {
        verifyCustomer(loginUserId);
        super.markDeleted(userName);

        registerEvent(new ReviewDeletedEvent(this.store.getId(), this.rating));
    }

    private void validateOrder(Order order) {
        if (!Objects.equals(order.getCustomerId(), customer.getId())) {
            throw new AppException(ReviewErrorCode.ORDER_REVIEWER_MISMATCH_DENIED, "주문자와 리뷰 작성자가 일치하지 않으면 리뷰 생성할 수 없습니다");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ReviewErrorCode.INVALID_ORDER_STATUS_DENIED, "주문이 완료되지 않은 경우 리뷰를 생성할 수 없습니다");
        }
    }

    private void verifyCustomer(Long loginUserId) {
        if (!loginUserId.equals(this.customer.getId())) {
            throw new AppException(ReviewErrorCode.REVIEW_UPDATE_DENIED, "리뷰 작성자만 수정 가능합니다");
        }
    }

    private void validateRating(int rating) {
        if (isOutOfRange(rating))
            throw new AppException(ReviewErrorCode.REVIEW_INVALID_RATING_VALUE, String.format("리뷰 점수는 %d부터 %d까지 입력할 수 있습니다.", MIN_RATING, MAX_RATING));
    }

    private boolean isOutOfRange(int rating) {
        return rating < MIN_RATING || rating > MAX_RATING;
    }
}
