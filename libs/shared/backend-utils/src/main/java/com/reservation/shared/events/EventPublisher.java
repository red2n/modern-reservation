package com.reservation.shared.events;

public interface EventPublisher {

    void publish(String topic, BaseEvent event);

    void publishAsync(String topic, BaseEvent event);
}
