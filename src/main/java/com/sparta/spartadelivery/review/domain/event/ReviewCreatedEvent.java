package com.sparta.spartadelivery.review.domain.event;

import java.util.UUID;

public record ReviewCreatedEvent(UUID storeId, int rating) {
}
