package com.modernreservation.reservationengine.infrastructure;

import com.reservation.shared.events.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Event Publisher Implementation for Avro Events
 *
 * Implements the EventPublisher interface using Spring Kafka.
 * Provides both synchronous and asynchronous event publishing capabilities.
 * Uses Avro serialization with Schema Registry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish Avro event synchronously
     * Blocks until the event is successfully sent or fails
     *
     * @param topic The Kafka topic to publish to
     * @param event The Avro SpecificRecord event to publish
     */
    @Override
    public void publish(String topic, SpecificRecord event) {
        try {
            String eventKey = extractEventKey(event);
            log.debug("Publishing Avro event synchronously - Topic: {}, EventType: {}, EventKey: {}",
                     topic, event.getClass().getSimpleName(), eventKey);

            SendResult<String, Object> result = kafkaTemplate.send(
                topic,
                eventKey,
                event
            ).get(); // Blocking call

            log.info("Avro event published successfully - Topic: {}, EventType: {}, EventKey: {}, Partition: {}, Offset: {}",
                    topic,
                    event.getClass().getSimpleName(),
                    eventKey,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Failed to publish Avro event synchronously - Topic: {}, EventType: {}, EventKey: {}",
                     topic, event.getClass().getSimpleName(), extractEventKey(event), e);
            throw new RuntimeException("Failed to publish Avro event to Kafka", e);
        }
    }

    /**
     * Publish Avro event asynchronously
     * Returns immediately without waiting for confirmation
     *
     * @param topic The Kafka topic to publish to
     * @param event The Avro SpecificRecord event to publish
     */
    @Override
    public void publishAsync(String topic, SpecificRecord event) {
        String eventKey = extractEventKey(event);
        log.debug("Publishing Avro event asynchronously - Topic: {}, EventType: {}, EventKey: {}",
                 topic, event.getClass().getSimpleName(), eventKey);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
            topic,
            eventKey,
            event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Avro event published successfully (async) - Topic: {}, EventType: {}, EventKey: {}, Partition: {}, Offset: {}",
                        topic,
                        event.getClass().getSimpleName(),
                        eventKey,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish Avro event asynchronously - Topic: {}, EventType: {}, EventKey: {}",
                         topic, event.getClass().getSimpleName(), eventKey, ex);
            }
        });
    }

    /**
     * Extract event key from Avro SpecificRecord
     * Gets 'eventId' field from schema (index 0)
     *
     * @param event Avro SpecificRecord
     * @return Event key (eventId if available, otherwise className-timestamp)
     */
    private String extractEventKey(SpecificRecord event) {
        try {
            // Get eventId field (first field in our schemas, index 0)
            org.apache.avro.Schema.Field field = event.getSchema().getField("eventId");
            if (field != null) {
                Object eventId = event.get(field.pos());
                if (eventId != null) {
                    return eventId.toString();
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract eventId from event, using fallback key", e);
        }
        return event.getClass().getSimpleName() + "-" + System.currentTimeMillis();
    }
}
