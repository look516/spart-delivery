package com.sparta.spartadelivery.payment.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.payment.application.AdminPaymentService;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class AdminPaymentController {

    private final AdminPaymentService adminService;

    @Operation(
            summary = "결제 상태 수정 API (only master & manager)"
    )
    @PutMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<?>> updatePayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID paymentId,
            PaymentStatus status
            ) {
        adminService.updatePaymentStatus(userPrincipal.getId(), paymentId, status);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "SUCCESS", null));
    }

    @Operation(
            summary = "결제 삭제 API (only master)"
    )
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<?>> deletePayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID paymentId
    ) {
        adminService.deletePayment(userPrincipal.getId(), paymentId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "SUCCESS", null));
    }
}
