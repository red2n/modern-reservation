package com.modernreservation.tenant.commons.enums;

/**
 * SubscriptionPlan - Subscription tier levels
 *
 * Matches TypeScript SubscriptionPlanSchema
 * Used for feature flags and billing
 */
public enum SubscriptionPlan {
    /**
     * Free tier - Limited properties and features
     */
    FREE,

    /**
     * Starter plan - Small properties (1-5 properties)
     */
    STARTER,

    /**
     * Professional plan - Medium properties (6-20 properties)
     */
    PROFESSIONAL,

    /**
     * Enterprise plan - Large chains (unlimited properties)
     */
    ENTERPRISE,

    /**
     * Custom enterprise plan - Negotiated features and pricing
     */
    CUSTOM
}
