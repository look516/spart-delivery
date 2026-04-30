package com.sparta.spartadelivery.review.domain.event;

import java.util.UUID;

public record ReviewUpdatedEvent(UUID storeId, int oldRating, int newRating) {
}
