package com.sparta.spartadelivery.store.application.listener;

import com.sparta.spartadelivery.review.domain.event.ReviewCreatedEvent;
import com.sparta.spartadelivery.review.domain.event.ReviewDeletedEvent;
import com.sparta.spartadelivery.review.domain.event.ReviewUpdatedEvent;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StoreEventListener {

    private final StoreRepository storeRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(ReviewCreatedEvent event) {
        // 평점 +N, 개수 +1
        storeRepository.updateStoreRating(event.storeId(), event.rating(), 1);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpdated(ReviewUpdatedEvent event) {
        // 평점 차이(새점수 - 옛점수)만큼 합계 수정, 개수 변동 없음(0)
        int delta = event.newRating() - event.oldRating();
        storeRepository.updateStoreRating(event.storeId(), delta, 0);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(ReviewDeletedEvent event) {
        // 평점 -N, 개수 -1
        storeRepository.updateStoreRating(event.storeId(), -event.rating(), -1);
    }
}
