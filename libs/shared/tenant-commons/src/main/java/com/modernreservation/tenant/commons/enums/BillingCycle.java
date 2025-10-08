package com.modernreservation.tenant.commons.enums;

/**
 * BillingCycle - Subscription billing frequency
 *
 * Matches TypeScript schema billingCycle enum
 */
public enum BillingCycle {
    /**
     * Monthly billing
     */
    MONTHLY,

    /**
     * Yearly billing (annual)
     */
    YEARLY,

    /**
     * Custom billing cycle (negotiated)
     */
    CUSTOM
}
