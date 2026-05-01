package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.order.domain.entity.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        @NotNull
        UUID storeId,

        @NotNull
        UUID addressId,

        @NotNull
        OrderType orderType,

        String request,

        @NotNull(message = "orderItem 은 존재해야 합니다.") @Valid
        List<OrderItemRequest> orderItems
) {


}
