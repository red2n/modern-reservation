package com.modernreservation.availabilitycalculator.kafka;

import com.modernreservation.availabilitycalculator.service.TenantCacheService;
import com.modernreservation.tenant.commons.dto.TenantCacheDTO;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.events.TenantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Kafka consumer for tenant events in Availability Calculator service.
 *
 * Listens to tenant-related events from Tenant Service and updates local cache.
 * This enables fast tenant validation during availability calculations without network calls.
 *
 * Topics:
 * - tenant.created     - New tenant created
 * - tenant.updated     - Tenant information updated
 * - tenant.deleted     - Tenant soft-deleted (blocks availability calculations)
 * - tenant.suspended   - Tenant suspended (blocks availability calculations)
 * - tenant.activated   - Tenant activated (enables availability calculations)
 * - tenant.expired     - Tenant subscription expired (blocks availability calculations)
 *
 * Event Flow:
 * Tenant Service → Kafka Topic → TenantEventConsumer → TenantCacheService → PostgreSQL → Redis Cache
 *
 * @see TenantCacheService
 * @see com.modernreservation.availabilitycalculator.entity.TenantCache
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEventConsumer {

    private final TenantCacheService tenantCacheService;

    /**
     * Handle tenant creation events.
     * Creates new tenant cache entry for fast availability validation.
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
            log.info("Successfully processed TENANT_CREATED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_CREATED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Handle tenant update events.
     * Updates cached tenant information for availability calculations.
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
     * Handle tenant deletion events.
     * Marks tenant as deleted - blocks all availability calculations.
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

        log.warn("Received TENANT_DELETED event for tenant: {} from topic: {}, partition: {}, offset: {}. " +
                 "Availability calculations will be BLOCKED.",
                 event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.markAsDeleted(event.getTenantId());
            log.info("Successfully processed TENANT_DELETED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_DELETED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Handle tenant suspension events.
     * Updates tenant status to SUSPENDED - blocks availability calculations.
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

        log.warn("Received TENANT_SUSPENDED event for tenant: {} from topic: {}, partition: {}, offset: {}. " +
                 "Availability calculations will be BLOCKED.",
                 event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.SUSPENDED);
            log.info("Successfully processed TENANT_SUSPENDED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_SUSPENDED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Handle tenant activation events.
     * Updates tenant status to ACTIVE - enables availability calculations.
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

        log.info("Received TENANT_ACTIVATED event for tenant: {} from topic: {}, partition: {}, offset: {}. " +
                 "Availability calculations now ENABLED.",
                 event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.ACTIVE);
            log.info("Successfully processed TENANT_ACTIVATED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_ACTIVATED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Handle tenant expiration events.
     * Updates tenant status to EXPIRED - blocks availability calculations.
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

        log.warn("Received TENANT_EXPIRED event for tenant: {} from topic: {}, partition: {}, offset: {}. " +
                 "Availability calculations will be BLOCKED.",
                 event.getTenantId(), topic, partition, offset);

        try {
            tenantCacheService.updateStatus(event.getTenantId(), TenantStatus.EXPIRED);
            log.info("Successfully processed TENANT_EXPIRED event: {}", event.getTenantId());
        } catch (Exception e) {
            log.error("Failed to process TENANT_EXPIRED event: {}", event.getTenantId(), e);
            throw e;
        }
    }

    /**
     * Map TenantEvent to TenantCacheDTO.
     * Only maps fields that exist in both objects for cache storage.
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
