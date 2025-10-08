package com.modernreservation.reservationengine.config;

import com.modernreservation.tenant.commons.events.TenantEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration for Tenant Events
 *
 * Configures Kafka consumers to listen to tenant events from Tenant Service.
 * Uses JSON deserialization to convert Kafka messages to TenantEvent objects.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:reservation-engine-tenant-consumer}")
    private String groupId;

    /**
     * Kafka consumer configuration
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.modernreservation.tenant.commons.events");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TenantEvent.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        return props;
    }

    /**
     * Consumer factory for TenantEvent
     */
    @Bean
    public ConsumerFactory<String, TenantEvent> consumerFactory() {
        JsonDeserializer<TenantEvent> deserializer = new JsonDeserializer<>(TenantEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.modernreservation.tenant.commons.events");
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }

    /**
     * Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TenantEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TenantEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 concurrent consumers
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
