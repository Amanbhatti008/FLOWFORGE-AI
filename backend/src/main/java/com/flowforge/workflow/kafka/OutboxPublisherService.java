package com.flowforge.workflow.kafka;

import com.flowforge.workflow.domain.OutboxEvent;
import com.flowforge.workflow.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxCallbackService outboxCallbackService;

    @Scheduled(fixedDelayString = "2000") // Runs every 2 seconds
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findRipeEvents(100);
        
        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} ripe outbox events to publish to Kafka", events.size());

        for (OutboxEvent event : events) {
            String topic = KafkaConfig.TOPIC_TASKS_EXECUTE; // Currently hardcoded as we only have one type of event
            String key = event.getAggregateId().toString(); // Use task ID as partition key to preserve ordering
            String payload = event.getPayload();

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);

            // We must process the callback properly
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    outboxCallbackService.handleSuccess(event.getId());
                } else {
                    outboxCallbackService.handleFailure(event.getId(), ex.getMessage());
                }
            });
        }
    }
}
