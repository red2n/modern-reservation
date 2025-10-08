package com.modernreservation.tenant.commons.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantEvent - Base class for all tenant-related Kafka events
 *
 * Published by Tenant Service and consumed by all business services
 * to keep local tenant cache in sync.
 *
 * Kafka Topics:
 * - tenant.created
 * - tenant.updated
 * - tenant.deleted
 * - tenant.suspended
 * - tenant.activated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEvent {

    /**
     * Type of event
     */
    private TenantEventType eventType;

    /**
     * Tenant ID
     */
    private UUID tenantId;

    /**
     * Tenant name
     */
    private String name;

    /**
     * Tenant slug (URL-friendly identifier)
     */
    private String slug;

    /**
     * Tenant type
     */
    private TenantType type;

    /**
     * Current tenant status
     */
    private TenantStatus status;

    /**
     * Tenant email
     */
    private String email;

    /**
     * Tenant phone (optional)
     */
    private String phone;

    /**
     * Subscription plan
     */
    private SubscriptionPlan subscriptionPlan;

    /**
     * When tenant was created
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * When tenant was last updated
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * When tenant was deleted (soft delete)
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    /**
     * When this event was published
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTimestamp;

    /**
     * Event sequence number for ordering
     */
    private Long eventSequence;

    /**
     * Additional metadata (optional)
     */
    private String metadata;

    /**
     * Event type enumeration
     */
    public enum TenantEventType {
        TENANT_CREATED,
        TENANT_UPDATED,
        TENANT_DELETED,
        TENANT_SUSPENDED,
        TENANT_ACTIVATED,
        TENANT_EXPIRED
    }

    /**
     * Check if tenant is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Check if tenant is active
     */
    public boolean isActive() {
        return TenantStatus.ACTIVE.equals(status) && !isDeleted();
    }
}
