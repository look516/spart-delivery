package com.sparta.spartadelivery.review.domain.entity;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewTest {

    @Mock
    private Order order;
    @Mock
    private Store store;
    @Mock
    private UserEntity customer;

    private final Long CUSTOMER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 대부분의 검증에서 유저 ID 비교가 필요하므로 미리 설정
        given(customer.getId()).willReturn(CUSTOMER_ID);
    }

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReview {

        @Test
        @DisplayName("성공: 모든 조건이 충족되면 리뷰가 정상 생성된다")
        void createReviewSuccess() {
            // given
            given(order.getCustomerId()).willReturn(CUSTOMER_ID);
            given(order.getStatus()).willReturn(OrderStatus.DELIVERED);

            // when
            Review review = new Review(order, store, customer, 5, "정말 맛있어요!");

            // then
            assertThat(review.getRating()).isEqualTo(5);
            assertThat(review.getContent()).isEqualTo("정말 맛있어요!");
        }

        @Test
        @DisplayName("예외: 주문자와 리뷰 작성자가 다르면 생성에 실패한다")
        void createReviewFail_UserMismatch() {
            // given
            given(order.getCustomerId()).willReturn(999L); // 다른 유저 ID

            // when & then
            assertThatThrownBy(() -> new Review(order, store, customer, 5, "실패 케이스"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("주문자와 리뷰 작성자가 일치하지 않으면");
        }

        @Test
        @DisplayName("예외: 주문 상태가 배달 완료(DELIVERED)가 아니면 생성에 실패한다")
        void createReviewFail_InvalidOrderStatus() {
            // given
            given(order.getCustomerId()).willReturn(CUSTOMER_ID);
            given(order.getStatus()).willReturn(OrderStatus.PENDING);

            // when & then
            assertThatThrownBy(() -> new Review(order, store, customer, 5, "실패 케이스"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("주문이 완료되지 않은 경우");
        }

        @Test
        @DisplayName("예외: 별점이 1~5 범위를 벗어나면 생성에 실패한다")
        void createReviewFail_InvalidRating() {
            // given
            given(order.getCustomerId()).willReturn(CUSTOMER_ID);
            given(order.getStatus()).willReturn(OrderStatus.DELIVERED);

            // when & then
            assertThatThrownBy(() -> new Review(order, store, customer, 0, "0점은 안돼요"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("리뷰 점수는 1부터 5까지");
        }
    }

    @Nested
    @DisplayName("리뷰 수정/삭제 테스트")
    class UpdateAndDeleteReview {

        private Review review;

        @BeforeEach
        void initReview() {
            given(order.getCustomerId()).willReturn(CUSTOMER_ID);
            given(order.getStatus()).willReturn(OrderStatus.DELIVERED);
            review = new Review(order, store, customer, 5, "원래 내용");
        }

        @Test
        @DisplayName("성공: 본인의 리뷰를 수정하면 내용이 변경된다")
        void updateReviewSuccess() {
            // when
            review.update(CUSTOMER_ID, 4, "변경된 내용");

            // then
            assertThat(review.getRating()).isEqualTo(4);
            assertThat(review.getContent()).isEqualTo("변경된 내용");
        }

        @Test
        @DisplayName("예외: 타인의 리뷰를 수정하려 하면 실패한다")
        void updateReviewFail_Forbidden() {
            // when & then
            assertThatThrownBy(() -> review.update(999L, 1, "남의 리뷰"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("리뷰 작성자만 수정 가능");
        }

        @Test
        @DisplayName("성공: 본인의 리뷰를 삭제하면 Soft Delete 처리된다")
        void deleteReviewSuccess() {
            // when
            review.delete(CUSTOMER_ID, "tester");

            // then
            assertThat(review.isDeleted()).isTrue();
            assertThat(review.getDeletedBy()).isEqualTo("tester");
        }
    }
}
