package com.modernreservation.tenantservice.entity;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Tenant Entity - Master tenant data
 *
 * Represents a tenant organization (hotel chain, franchise, independent property, etc.)
 * Uses JSONB for complex nested structures (address, business info, config, subscription)
 *
 * This is the single source of truth for all tenant data.
 * Changes to this entity trigger Kafka events to sync with other services.
 */
@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenants_slug", columnList = "slug", unique = true),
    @Index(name = "idx_tenants_type", columnList = "type"),
    @Index(name = "idx_tenants_status", columnList = "status"),
    @Index(name = "idx_tenants_deleted", columnList = "deleted_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TenantType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TenantStatus status;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String website;

    /**
     * Address stored as JSONB
     * Structure: { street, city, state, country, postalCode, latitude, longitude }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> address;

    /**
     * Business information stored as JSONB
     * Structure: { legalName, taxId, registrationNumber, industry, numberOfEmployees, yearEstablished }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_info", columnDefinition = "jsonb")
    private Map<String, Object> businessInfo;

    /**
     * Configuration and feature flags stored as JSONB
     * Structure: {
     *   branding: { enabled, logoUrl, primaryColor, secondaryColor },
     *   features: { multiProperty, channelManager, advancedReporting, paymentProcessing, loyaltyProgram },
     *   limits: { maxProperties, maxUsers, maxReservationsPerMonth },
     *   localization: { currency, language, timezone },
     *   notifications: { email, sms, push }
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> config;

    /**
     * Subscription details stored as JSONB
     * Structure: {
     *   plan, startDate, endDate, billingCycle, billingEmail, billingAddress,
     *   isActive, trialEndsAt, nextBillingDate, autoRenew,
     *   pricing: { monthlyPrice, yearlyPrice, currency },
     *   payment: { lastPaymentDate, lastPaymentAmount, nextPaymentAmount },
     *   usage: { propertiesUsed, usersUsed, reservationsThisMonth }
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> subscription;

    /**
     * Additional metadata stored as JSONB
     * Flexible structure for custom fields
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
        return status == TenantStatus.ACTIVE && !isDeleted();
    }

    /**
     * Get subscription plan from subscription JSONB
     */
    public SubscriptionPlan getSubscriptionPlan() {
        if (subscription != null && subscription.containsKey("plan")) {
            String plan = (String) subscription.get("plan");
            return SubscriptionPlan.valueOf(plan);
        }
        return SubscriptionPlan.FREE;
    }

    /**
     * Soft delete the tenant
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = TenantStatus.CANCELLED;
    }

    /**
     * Restore soft-deleted tenant
     */
    public void restore() {
        this.deletedAt = null;
        this.status = TenantStatus.ACTIVE;
    }

    /**
     * Suspend tenant
     */
    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    /**
     * Activate tenant
     */
    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    /**
     * Expire tenant subscription
     */
    public void expire() {
        this.status = TenantStatus.EXPIRED;
    }
}
