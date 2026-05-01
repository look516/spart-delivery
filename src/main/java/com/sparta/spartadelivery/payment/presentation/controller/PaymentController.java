package com.sparta.spartadelivery.payment.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.payment.application.PaymentService;
import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentRequest;
import com.sparta.spartadelivery.payment.presentation.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "결제 요청 API",
            description = "현재는 결제 내역을 DB 에 저장만 하는 형태입니다."
    )
    @PostMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PaymentRequest request,
            @PathVariable UUID orderId) {

        PaymentResponse response = paymentService.processPayment(userPrincipal.getId(), orderId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", response));

    }
}
