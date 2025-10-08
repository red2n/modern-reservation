package com.modernreservation.paymentprocessor.kafka;

import com.modernreservation.paymentprocessor.service.TenantCacheService;
import com.modernreservation.tenant.commons.events.TenantEvent;
import com.modernreservation.tenant.commons.dto.TenantCacheDTO;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * TenantEventConsumer - Kafka listener for tenant events in Payment Processor
 *
 * CRITICAL: This consumer is essential for financial data security.
 * It ensures the payment processor has up-to-date tenant status information
 * to prevent unauthorized or invalid payment processing.
 *
 * This component listens to Kafka topics published by the Tenant Service
 * and updates the local tenant cache accordingly.
 *
 * Kafka Topics:
 * - tenant.created     - New tenant created (enable payments)
 * - tenant.updated     - Tenant information updated
 * - tenant.deleted     - Tenant soft-deleted (block new payments)
 * - tenant.suspended   - Tenant suspended (block new payments)
 * - tenant.activated   - Tenant activated (enable payments)
 * - tenant.expired     - Tenant subscription expired (block new payments)
 *
 * Event Flow:
 * 1. Tenant Service makes change to tenant data
 * 2. Tenant Service publishes Kafka event
 * 3. This consumer receives the event
 * 4. Local tenant cache is updated
 * 5. Payment processing uses updated cache for security validation
 *
 * Security Benefits:
 * - No HTTP calls to Tenant Service needed during payment processing
 * - Fast local cache access for tenant validation
 * - Immediate tenant status enforcement
 * - Payment blocking for suspended/cancelled tenants
 * - Decoupled architecture with eventual consistency
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEventConsumer {

    private final TenantCacheService tenantCacheService;

    /**
     * Process tenant creation events
     * Cache new tenant for fast payment validation
     */
    @KafkaListener(
            topics = "tenant.created",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantCreated(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.info("Received TENANT_CREATED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            TenantCacheDTO cacheDTO = mapToTenantCacheDTO(event);
            tenantCacheService.saveOrUpdate(cacheDTO);
            log.info("Successfully processed TENANT_CREATED event: {} - payments can now be processed",
                     event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_CREATED event: {}", event.getTenantId(), e);
            throw e; // Rethrow to allow retry mechanism
        }
    }

    /**
     * Process tenant update events
     * Update cached tenant information for payment validation
     */
    @KafkaListener(
            topics = "tenant.updated",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantUpdated(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.info("Received TENANT_UPDATED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            TenantCacheDTO cacheDTO = mapToTenantCacheDTO(event);
            tenantCacheService.saveOrUpdate(cacheDTO);
            log.info("Successfully processed TENANT_UPDATED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_UPDATED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Process tenant deletion events
     * Mark tenant as deleted - BLOCKS NEW PAYMENTS
     */
    @KafkaListener(
            topics = "tenant.deleted",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantDeleted(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.warn("SECURITY: Received TENANT_DELETED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            LocalDateTime deletedAt = event.getDeletedAt() != null
                ? event.getDeletedAt()
                : LocalDateTime.now();
            tenantCacheService.markAsDeleted(event.getTenantId(), deletedAt);
            log.warn("SECURITY: Successfully processed TENANT_DELETED event: {} - NEW PAYMENTS BLOCKED",
                     event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_DELETED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Process tenant suspension events
     * Update tenant status to SUSPENDED - BLOCKS NEW PAYMENTS
     */
    @KafkaListener(
            topics = "tenant.suspended",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantSuspended(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.warn("SECURITY: Received TENANT_SUSPENDED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.SUSPENDED);
            log.warn("SECURITY: Successfully processed TENANT_SUSPENDED event: {} - NEW PAYMENTS BLOCKED",
                     event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_SUSPENDED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Process tenant activation events
     * Update tenant status to ACTIVE - ENABLES PAYMENTS
     */
    @KafkaListener(
            topics = "tenant.activated",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantActivated(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.info("SECURITY: Received TENANT_ACTIVATED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.ACTIVE);
            log.info("SECURITY: Successfully processed TENANT_ACTIVATED event: {} - PAYMENTS ENABLED",
                     event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_ACTIVATED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Process tenant expiration events
     * Update tenant status to EXPIRED - BLOCKS NEW PAYMENTS
     */
    @KafkaListener(
            topics = "tenant.expired",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantEventKafkaListenerContainerFactory"
    )
    public void handleTenantExpired(
            @Payload TenantEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {

        log.warn("SECURITY: Received TENANT_EXPIRED event for tenant: {} from topic: {}, partition: {}, offset: {}",
                event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.EXPIRED);
            log.warn("SECURITY: Successfully processed TENANT_EXPIRED event: {} - NEW PAYMENTS BLOCKED",
                     event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_EXPIRED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Map TenantEvent to TenantCacheDTO
     * Only maps fields that exist in both objects
     */
    private TenantCacheDTO mapToTenantCacheDTO(TenantEvent event) {
        return TenantCacheDTO.builder()
                .tenantId(event.getTenantId())
                .name(event.getName())
                .slug(event.getSlug())
                .type(event.getType())
                .status(event.getStatus())
                .subscriptionPlan(event.getSubscriptionPlan())
                .deletedAt(event.getDeletedAt())
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
