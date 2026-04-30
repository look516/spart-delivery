package com.sparta.spartadelivery.review.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReviewErrorCode implements BaseErrorCode {
    REVIEW_INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "선택 가능한 리뷰 평점 범위를 벗어났습니다"),
    ORDER_REVIEWER_MISMATCH_DENIED(HttpStatus.FORBIDDEN, "주문자와 리뷰 작성자가 일치하지 않으면 리뷰 생성할 수 없습니다"),
    INVALID_ORDER_STATUS_DENIED(HttpStatus.FORBIDDEN, "주문이 완료되지 않은 경우 리뷰를 생성할 수 없습니다"),
    REVIEW_UPDATE_DENIED(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정 가능합니다");

    private final HttpStatus status;
    private final String message;

    ReviewErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
