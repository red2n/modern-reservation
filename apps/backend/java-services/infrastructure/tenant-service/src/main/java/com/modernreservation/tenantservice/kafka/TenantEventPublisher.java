package com.modernreservation.tenantservice.kafka;

import com.modernreservation.tenant.commons.dto.TenantEvent;
import com.modernreservation.tenant.commons.enums.TenantEventType;
import com.modernreservation.tenantservice.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Tenant Event Publisher
 *
 * Publishes Kafka events when tenant data changes.
 * Other microservices subscribe to these events to update their local tenant caches.
 *
 * Event Types:
 * - TENANT_CREATED: New tenant registered
 * - TENANT_UPDATED: Tenant details changed
 * - TENANT_DELETED: Tenant soft deleted
 * - TENANT_SUSPENDED: Tenant account suspended
 * - TENANT_ACTIVATED: Tenant account activated
 * - TENANT_EXPIRED: Subscription expired
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantEventPublisher {

    private final KafkaTemplate<String, TenantEvent> kafkaTemplate;

    @Value("${kafka.topics.tenant-created}")
    private String tenantCreatedTopic;

    @Value("${kafka.topics.tenant-updated}")
    private String tenantUpdatedTopic;

    @Value("${kafka.topics.tenant-deleted}")
    private String tenantDeletedTopic;

    @Value("${kafka.topics.tenant-suspended}")
    private String tenantSuspendedTopic;

    @Value("${kafka.topics.tenant-activated}")
    private String tenantActivatedTopic;

    @Value("${kafka.topics.tenant-expired}")
    private String tenantExpiredTopic;

    /**
     * Publish tenant created event
     */
    public void publishTenantCreated(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_CREATED);
        publishEvent(tenantCreatedTopic, event);
        log.info("Published TENANT_CREATED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Publish tenant updated event
     */
    public void publishTenantUpdated(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_UPDATED);
        publishEvent(tenantUpdatedTopic, event);
        log.info("Published TENANT_UPDATED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Publish tenant deleted event
     */
    public void publishTenantDeleted(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_DELETED);
        publishEvent(tenantDeletedTopic, event);
        log.info("Published TENANT_DELETED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Publish tenant suspended event
     */
    public void publishTenantSuspended(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_SUSPENDED);
        publishEvent(tenantSuspendedTopic, event);
        log.info("Published TENANT_SUSPENDED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Publish tenant activated event
     */
    public void publishTenantActivated(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_ACTIVATED);
        publishEvent(tenantActivatedTopic, event);
        log.info("Published TENANT_ACTIVATED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Publish tenant expired event
     */
    public void publishTenantExpired(Tenant tenant) {
        TenantEvent event = buildEvent(tenant, TenantEventType.TENANT_EXPIRED);
        publishEvent(tenantExpiredTopic, event);
        log.info("Published TENANT_EXPIRED event for tenant: {} ({})", tenant.getName(), tenant.getId());
    }

    /**
     * Build Tenant Event DTO from Tenant entity
     */
    private TenantEvent buildEvent(Tenant tenant, TenantEventType eventType) {
        return TenantEvent.builder()
                .eventType(eventType)
                .tenantId(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .type(tenant.getType())
                .status(tenant.getStatus())
                .subscriptionPlan(tenant.getSubscriptionPlan())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .deletedAt(tenant.getDeletedAt())
                .eventTimestamp(LocalDateTime.now())
                .metadata(tenant.getMetadata())
                .build();
    }

    /**
     * Publish event to Kafka topic
     */
    private void publishEvent(String topic, TenantEvent event) {
        try {
            // Use tenant ID as partition key for ordering
            String key = event.getTenantId().toString();

            CompletableFuture<SendResult<String, TenantEvent>> future =
                kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Event published successfully to topic: {} with offset: {}",
                             topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event to topic: {}", topic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing tenant event to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish tenant event", e);
        }
    }
}
