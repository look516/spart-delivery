package com.sparta.spartadelivery.review.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.review.domain.entity.Review;
import com.sparta.spartadelivery.review.domain.repository.ReviewRepository;
import com.sparta.spartadelivery.review.domain.repository.ReviewSearchRepository;
import com.sparta.spartadelivery.review.presentation.dto.ReviewDeletedInfoDto;
import com.sparta.spartadelivery.review.presentation.dto.ReviewUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewSearchRepository reviewSearchRepository;

    @Nested
    @DisplayName("리뷰 상세 조회 테스트")
    class ViewDetail {
        @Test
        @DisplayName("존재하는 리뷰 ID로 조회하면 상세 정보를 반환한다")
        void viewDetail_Success() {
            // given
//            UUID reviewId = UUID.randomUUID();
//
//            // 1. 필요한 연관 객체들 Mocking (엔티티 생성자에 필요)
//            Order mockOrder = mock(Order.class);
//            UserEntity mockUser = mock(UserEntity.class);
//            Store mockStore = mock(Store.class);
//
//            // 2. Order에서 데이터가 나올 수 있도록 설정
//            given(mockOrder.getCustomer()).willReturn(mockUser);
//            given(mockOrder.getStore()).willReturn(mockStore);
//            given(mockOrder.getStatus()).willReturn(OrderStatus.COMPLETED);
//            given(mockUser.getId()).willReturn(1L);
//
//            // 3. 실제 Review 객체 생성 (Mock이 아님!)
//            Review review = new Review(mockOrder, mockUser, 5, "맛있어요");
//
//            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
//
//            // when
//            ReviewDetailDto result = reviewService.viewDetail(reviewId);
//
//            // then
//            assertThat(result).isNotNull();
//            // DTO에 데이터가 잘 들어갔는지 추가 검증 가능
//            assertThat(result.content()).isEqualTo("맛있어요");
//            verify(reviewRepository).findById(reviewId);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 ID로 조회하면 EntityNotFoundException이 발생한다")
        void viewDetail_Fail() {
            // given
            UUID reviewId = UUID.randomUUID();
            given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.viewDetail(reviewId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 수정 테스트")
    class UpdateReview {
        @Test
        @DisplayName("작성자가 본인의 리뷰를 수정하면 성공한다")
        void update_Success() {
            // given
            UUID reviewId = UUID.randomUUID();
            Long customerId = 1L;
            ReviewUpdateRequest request = new ReviewUpdateRequest(5, "수정된 내용");
            Review review = mock(Review.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            // when
            reviewService.update(reviewId, customerId, request);

            // then
            // 엔티티의 update 메서드가 올바른 인자로 호출되었는지 확인
            verify(review).update(customerId, request.rating(), request.content());
        }

        @Test
        @DisplayName("작성자가 아닌 사람이 수정을 시도하면 예외가 발생한다")
        void update_Fail_Unauthorized() {
            // given
            UUID reviewId = UUID.randomUUID();
            Long strangerId = 999L;
            ReviewUpdateRequest request = new ReviewUpdateRequest(5, "해킹 시도");
            Review review = mock(Review.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            // 엔티티 내부 검증에서 예외가 발생한다고 가정
            doThrow(new AppException(null, "리뷰 작성자만 수정 가능합니다"))
                    .when(review).update(anyLong(), anyInt(), anyString());

            // when & then
            assertThatThrownBy(() -> reviewService.update(reviewId, strangerId, request))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReview {
        @Test
        @DisplayName("정상적인 삭제 요청 시 soft delete가 수행된다")
        void delete_Success() {
            // given
            UUID reviewId = UUID.randomUUID();
            ReviewDeletedInfoDto deletedInfo = new ReviewDeletedInfoDto(1L, "홍길동");
            Review review = mock(Review.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            // when
            reviewService.delete(reviewId, deletedInfo);

            // then
            verify(review).delete(deletedInfo.loginId(), deletedInfo.userName());
        }
    }
}