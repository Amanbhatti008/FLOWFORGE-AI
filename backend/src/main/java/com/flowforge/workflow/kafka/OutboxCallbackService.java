package com.flowforge.workflow.kafka;

import com.flowforge.workflow.repository.OutboxEventRepository;
import com.flowforge.workflow.statemachine.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OutboxCallbackService {

    private final OutboxEventRepository outboxEventRepository;
    private static final int MAX_RETRIES = 5;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSuccess(java.util.UUID eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxStatus.PROCESSED);
            event.setPublished(true);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
            log.debug("Successfully published outbox event {} to Kafka", eventId);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailure(java.util.UUID eventId, String error) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);
            event.setErrorMessage(error);
            
            if (newRetryCount >= MAX_RETRIES) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("Outbox event {} failed permanently after {} retries. Error: {}", eventId, MAX_RETRIES, error);
            } else {
                event.setStatus(OutboxStatus.FAILED);
                long backoffSeconds = (long) Math.pow(2, newRetryCount);
                event.setNextRetryAt(Instant.now().plus(backoffSeconds, ChronoUnit.SECONDS));
                log.warn("Outbox event {} failed to publish. Scheduled retry {}/{} in {}s. Error: {}", 
                        eventId, newRetryCount, MAX_RETRIES, backoffSeconds, error);
            }
            outboxEventRepository.save(event);
        });
    }
}
