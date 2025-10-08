package com.modernreservation.tenantservice.dto;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for tenant data
 * Returns full tenant information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

    private UUID id;
    private String name;
    private String slug;
    private TenantType type;
    private TenantStatus status;
    private String email;
    private String phone;
    private String website;

    private Map<String, Object> address;
    private Map<String, Object> businessInfo;
    private Map<String, Object> config;
    private Map<String, Object> subscription;
    private Map<String, Object> metadata;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime deletedAt;

    // Helper fields
    private boolean isDeleted;
    private boolean isActive;
    private SubscriptionPlan subscriptionPlan;
}
