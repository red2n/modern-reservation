package com.reservation.shared.events;

import org.apache.avro.specific.SpecificRecord;

/**
 * Event Publisher interface for publishing domain events to Kafka
 * Now supports Avro SpecificRecord types
 */
public interface EventPublisher {

    /**
     * Publish an Avro event synchronously
     * @param topic Kafka topic name
     * @param event Avro SpecificRecord event
     */
    void publish(String topic, SpecificRecord event);

    /**
     * Publish an Avro event asynchronously
     * @param topic Kafka topic name
     * @param event Avro SpecificRecord event
     */
    void publishAsync(String topic, SpecificRecord event);
}
