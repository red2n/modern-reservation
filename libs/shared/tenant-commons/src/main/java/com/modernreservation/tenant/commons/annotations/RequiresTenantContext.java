package com.modernreservation.tenant.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequiresTenantContext - Annotation to mark methods that require tenant context
 *
 * Methods annotated with this will be validated by AOP to ensure
 * TenantContext.getCurrentTenantId() is available and tenant is active.
 *
 * Usage:
 * <pre>
 * {@literal @}RequiresTenantContext
 * public Reservation createReservation(ReservationRequest request) {
 *     // Tenant context is guaranteed to be set
 *     UUID tenantId = TenantContext.getCurrentTenantId();
 *     // ...
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTenantContext {

    /**
     * Whether to validate tenant is active
     * Default: true
     */
    boolean validateActive() default true;

    /**
     * Custom error message if tenant context is missing
     */
    String message() default "Tenant context required but not found";
}
