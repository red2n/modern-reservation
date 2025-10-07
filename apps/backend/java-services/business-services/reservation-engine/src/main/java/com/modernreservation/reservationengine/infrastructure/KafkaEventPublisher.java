package com.modernreservation.reservationengine.infrastructure;

import com.reservation.shared.events.BaseEvent;
import com.reservation.shared.events.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Event Publisher Implementation
 *
 * Implements the EventPublisher interface using Spring Kafka.
 * Provides both synchronous and asynchronous event publishing capabilities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish event synchronously
     * Blocks until the event is successfully sent or fails
     *
     * @param topic The Kafka topic to publish to
     * @param event The event to publish
     */
    @Override
    public void publish(String topic, BaseEvent event) {
        try {
            log.debug("Publishing event synchronously - Topic: {}, EventType: {}, EventId: {}",
                     topic, event.getEventType(), event.getEventId());

            SendResult<String, Object> result = kafkaTemplate.send(
                topic,
                event.getEventId(),
                event
            ).get(); // Blocking call

            log.info("Event published successfully - Topic: {}, EventType: {}, EventId: {}, Partition: {}, Offset: {}",
                    topic,
                    event.getEventType(),
                    event.getEventId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Failed to publish event synchronously - Topic: {}, EventType: {}, EventId: {}",
                     topic, event.getEventType(), event.getEventId(), e);
            throw new RuntimeException("Failed to publish event to Kafka", e);
        }
    }

    /**
     * Publish event asynchronously
     * Returns immediately without waiting for confirmation
     *
     * @param topic The Kafka topic to publish to
     * @param event The event to publish
     */
    @Override
    public void publishAsync(String topic, BaseEvent event) {
        log.debug("Publishing event asynchronously - Topic: {}, EventType: {}, EventId: {}",
                 topic, event.getEventType(), event.getEventId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
            topic,
            event.getEventId(),
            event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published successfully (async) - Topic: {}, EventType: {}, EventId: {}, Partition: {}, Offset: {}",
                        topic,
                        event.getEventType(),
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event asynchronously - Topic: {}, EventType: {}, EventId: {}",
                         topic, event.getEventType(), event.getEventId(), ex);
            }
        });
    }
}
