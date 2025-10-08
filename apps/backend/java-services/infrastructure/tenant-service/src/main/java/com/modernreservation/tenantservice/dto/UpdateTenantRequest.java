package com.modernreservation.tenantservice.dto;

import com.modernreservation.tenant.commons.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing tenant
 * All fields are optional - only provided fields will be updated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequest {

    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    private String slug;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    private String website;

    private Map<String, Object> address;

    private Map<String, Object> businessInfo;

    private Map<String, Object> config;

    private Map<String, Object> metadata;
}
