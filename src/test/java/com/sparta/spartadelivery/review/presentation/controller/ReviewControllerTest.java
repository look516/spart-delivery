package com.sparta.spartadelivery.review.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JsonSecurityErrorResponder;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.review.exception.ReviewErrorCode;
import com.sparta.spartadelivery.review.presentation.dto.ReviewDetailDto;
import com.sparta.spartadelivery.review.presentation.dto.ReviewSearchCondition;
import com.sparta.spartadelivery.review.presentation.dto.ReviewUpdateRequest;
import com.sparta.spartadelivery.review.service.ReviewService;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JsonSecurityErrorResponder jsonSecurityErrorResponder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("리뷰 1건을 조회한다")
    void getViewDetailSuccess() throws Exception {

        UUID reviewId = UUID.randomUUID();
        ReviewDetailDto response = new ReviewDetailDto(reviewId, UUID.randomUUID(), 1L, 5, "최고의 맛!");
        given(reviewService.viewDetail(reviewId)).willReturn(response);

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.data.content").value("최고의 맛!"));
    }

    @Test
    @DisplayName("실패: 리뷰 작성자가 아닌 유저가 수정을 시도하면 에러를 반환한다")
    void updateReviewFail_NotAuthor() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        Long otherUserId = 999L; // 작성자가 아닌 다른 유저 ID
        ReviewUpdateRequest request = new ReviewUpdateRequest(4, "몰래 수정 시도");

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(otherUserId)
                .role(Role.CUSTOMER)
                .build();

        // 서비스에서 작성자 불일치 예외를 던지도록 설정
        willThrow(new AppException(ReviewErrorCode.REVIEW_UPDATE_DENIED, "리뷰 작성자만 수정 가능합니다"))
                .given(reviewService)
                .update(eq(reviewId), eq(otherUserId), any(ReviewUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
                        .with(csrf())
                        .with(user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("리뷰 작성자만 수정 가능합니다"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰를 조회한다")
    void searchReviewsSuccess() throws Exception {

        UUID storeId = UUID.randomUUID();
        ReviewDetailDto dto = new ReviewDetailDto(UUID.randomUUID(), UUID.randomUUID(), 1L, 5, "맛있어요~");
        SliceImpl<ReviewDetailDto> slice = new SliceImpl<>(List.of(dto), PageRequest.of(0, 10), false);

        given(reviewService.search(any(ReviewSearchCondition.class))).willReturn(slice);

        mockMvc.perform(get("/api/v1/reviews")
                        .param("storeId", storeId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("맛있어요~"));

    }
}