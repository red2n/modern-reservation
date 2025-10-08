package com.modernreservation.tenantservice.dto;

import com.modernreservation.tenant.commons.enums.TenantStatus;
import com.modernreservation.tenant.commons.enums.TenantType;
import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new tenant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    private String slug;

    @NotNull(message = "Tenant type is required")
    private TenantType type;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    private String website;

    @NotNull(message = "Address is required")
    private Map<String, Object> address;

    @NotNull(message = "Business information is required")
    private Map<String, Object> businessInfo;

    private Map<String, Object> config;

    @NotNull(message = "Subscription details are required")
    private SubscriptionRequest subscription;

    private Map<String, Object> metadata;

    /**
     * Nested subscription request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionRequest {
        @NotNull(message = "Subscription plan is required")
        private SubscriptionPlan plan;

        @NotBlank(message = "Billing email is required")
        @Email(message = "Invalid billing email format")
        private String billingEmail;

        private Map<String, Object> billingAddress;

        @Builder.Default
        private Boolean autoRenew = true;

        private Map<String, Object> pricing;
    }
}
