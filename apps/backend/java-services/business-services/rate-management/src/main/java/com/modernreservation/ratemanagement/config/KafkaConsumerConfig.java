package com.modernreservation.ratemanagement.config;

import com.modernreservation.tenant.commons.events.TenantEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration for Tenant Events in Rate Management
 *
 * Configures Kafka consumers to listen to tenant-related events from the Tenant Service.
 * Uses JSON deserialization with error handling for robust event processing.
 *
 * Key Features:
 * - Auto-offset reset to earliest for reliability
 * - JSON deserialization with trusted packages
 * - Error handling deserializer for resilience
 * - Concurrent message processing support
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Consumer factory for TenantEvent messages
     */
    @Bean
    public ConsumerFactory<String, TenantEvent> tenantEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        JsonDeserializer<TenantEvent> deserializer = new JsonDeserializer<>(TenantEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.modernreservation.tenant.commons.events");
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    /**
     * Kafka listener container factory for TenantEvent messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TenantEvent> tenantEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TenantEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tenantEventConsumerFactory());
        factory.setConcurrency(3); // 3 concurrent consumers
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);

        log.info("Configured TenantEvent Kafka listener container factory with group: {}", groupId);
        return factory;
    }
}
