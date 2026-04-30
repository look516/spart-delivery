package com.sparta.spartadelivery.review.domain.event;

import java.util.UUID;

public record ReviewDeletedEvent(UUID storeId, int rating) {
}
