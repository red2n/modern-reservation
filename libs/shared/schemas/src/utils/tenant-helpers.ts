import { type Tenant, type TenantContext, TenantContextSchema } from '../entities/domains/tenant';

// =============================================================================
// TENANT VALIDATION HELPERS
// =============================================================================

/**
 * Validates if a user has access to a specific tenant resource
 * @param context - The tenant context from the request
 * @param resourceTenantId - The tenant ID of the resource being accessed
 * @returns boolean - true if access is allowed
 * @throws Error if access is denied
 */
export function validateTenantAccess(context: TenantContext, resourceTenantId: string): boolean {
  if (!context.tenantId) {
    throw new Error('No tenant context available');
  }

  if (context.tenantId !== resourceTenantId) {
    throw new Error(
      `Access denied: Resource belongs to different tenant. ` +
        `User tenant: ${context.tenantId}, Resource tenant: ${resourceTenantId}`
    );
  }

  return true;
}

/**
 * Safely validates tenant access without throwing errors
 * @param context - The tenant context from the request
 * @param resourceTenantId - The tenant ID of the resource being accessed
 * @returns boolean - true if access is allowed, false otherwise
 */
export function hasTenantAccess(context: TenantContext, resourceTenantId: string): boolean {
  try {
    return validateTenantAccess(context, resourceTenantId);
  } catch {
    return false;
  }
}

/**
 * Extracts tenant context from a request object
 * @param request - The request object (could be Express, GraphQL context, etc.)
 * @returns TenantContext or null if not found
 */
export function getTenantFromContext(request: any): TenantContext | null {
  // Try different common locations for tenant context
  if (request.tenantContext) {
    return TenantContextSchema.parse(request.tenantContext);
  }

  if (request.context?.tenant) {
    return TenantContextSchema.parse(request.context.tenant);
  }

  if (request.user?.tenantContext) {
    return TenantContextSchema.parse(request.user.tenantContext);
  }

  if (request.headers?.['x-tenant-id']) {
    // Basic context from header
    return {
      tenantId: request.headers['x-tenant-id'],
      tenantName: request.headers['x-tenant-name'] || 'Unknown',
      tenantType: 'INDEPENDENT', // Default
      subscriptionPlan: 'FREE', // Default
      isActive: true,
      features: {
        multiProperty: false,
        channelManager: false,
        advancedReporting: false,
        paymentProcessing: true,
        loyaltyProgram: false,
      },
    };
  }

  return null;
}

/**
 * Checks if a tenant is currently active
 * @param tenant - The tenant object to check
 * @returns boolean - true if tenant is active
 */
export function isTenantActive(tenant: Tenant): boolean {
  return tenant.status === 'ACTIVE';
}

/**
 * Checks if a tenant is on trial
 * @param tenant - The tenant object to check
 * @returns boolean - true if tenant is on trial
 */
export function isTenantOnTrial(tenant: Tenant): boolean {
  return tenant.status === 'TRIAL';
}

/**
 * Checks if a tenant is suspended or expired
 * @param tenant - The tenant object to check
 * @returns boolean - true if tenant cannot access the system
 */
export function isTenantBlocked(tenant: Tenant): boolean {
  return ['SUSPENDED', 'EXPIRED', 'CANCELLED'].includes(tenant.status);
}

/**
 * Checks if a tenant has a specific feature enabled
 * @param tenant - The tenant object to check
 * @param feature - The feature key to check (e.g., 'multiProperty', 'channelManager')
 * @returns boolean - true if feature is enabled
 */
export function hasTenantFeature(tenant: Tenant, feature: string): boolean {
  if (!tenant.config) {
    return false;
  }

  // Map feature names to config properties
  const featureMap: Record<string, keyof typeof tenant.config> = {
    multiProperty: 'enableMultiProperty',
    channelManager: 'enableChannelManager',
    advancedReporting: 'enableAdvancedReporting',
    paymentProcessing: 'enablePaymentProcessing',
    loyaltyProgram: 'enableLoyaltyProgram',
  };

  const configKey = featureMap[feature];
  if (!configKey) {
    return false;
  }

  const featureValue = tenant.config[configKey];
  return featureValue === true;
}

/**
 * Checks if tenant has reached a specific limit
 * @param tenant - The tenant object to check
 * @param limitKey - The limit key (e.g., 'maxProperties', 'maxUsers', 'maxReservationsPerMonth')
 * @param currentValue - The current usage value
 * @returns boolean - true if limit is reached
 */
export function hasTenantReachedLimit(
  tenant: Tenant,
  limitKey: string,
  currentValue: number
): boolean {
  if (!tenant.config) {
    return false;
  }

  const limit = tenant.config[limitKey as keyof typeof tenant.config];
  if (typeof limit !== 'number') {
    return false;
  }

  return currentValue >= limit;
}

/**
 * Gets the remaining capacity for a tenant limit
 * @param tenant - The tenant object to check
 * @param limitKey - The limit key (e.g., 'maxProperties', 'maxUsers', 'maxReservationsPerMonth')
 * @param currentValue - The current usage value
 * @returns number - remaining capacity, or Infinity if no limit
 */
export function getTenantRemainingCapacity(
  tenant: Tenant,
  limitKey: string,
  currentValue: number
): number {
  if (!tenant.config) {
    return Infinity;
  }

  const limit = tenant.config[limitKey as keyof typeof tenant.config];
  if (typeof limit !== 'number') {
    return Infinity;
  }

  return Math.max(0, limit - currentValue);
}

/**
 * Validates if a tenant can perform an action based on subscription plan
 * @param tenant - The tenant object to check
 * @param requiredPlans - Array of subscription plans that allow this action
 * @returns boolean - true if tenant's plan is in the required plans
 */
export function hasRequiredSubscription(
  tenant: Tenant,
  requiredPlans: Array<'FREE' | 'STARTER' | 'PROFESSIONAL' | 'ENTERPRISE' | 'CUSTOM'>
): boolean {
  if (!tenant.subscription?.plan) {
    return requiredPlans.includes('FREE');
  }

  return requiredPlans.includes(tenant.subscription.plan);
}

/**
 * Checks if tenant's subscription is active (not expired)
 * @param tenant - The tenant object to check
 * @returns boolean - true if subscription is active
 */
export function isSubscriptionActive(tenant: Tenant): boolean {
  if (!tenant.subscription || !tenant.subscription.endDate) {
    return false;
  }

  const now = new Date();
  const endDate = new Date(tenant.subscription.endDate);

  return endDate > now;
}

/**
 * Gets days remaining until subscription expires
 * @param tenant - The tenant object to check
 * @returns number - days remaining, or 0 if expired/no subscription
 */
export function getSubscriptionDaysRemaining(tenant: Tenant): number {
  if (!tenant.subscription || !tenant.subscription.endDate) {
    return 0;
  }

  const now = new Date();
  const endDate = new Date(tenant.subscription.endDate);
  const diffTime = endDate.getTime() - now.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  return Math.max(0, diffDays);
}

/**
 * Formats tenant ID for logging (truncated for security)
 * @param tenantId - The tenant ID to format
 * @returns string - truncated tenant ID
 */
export function formatTenantIdForLog(tenantId: string): string {
  if (tenantId.length <= 8) {
    return tenantId;
  }
  return `${tenantId.substring(0, 8)}...`;
}

/**
 * Creates a tenant context object for testing/development
 * @param tenantId - The tenant ID
 * @param options - Additional options for the context
 * @returns TenantContext
 */
export function createTenantContext(
  tenantId: string,
  options?: {
    tenantName?: string;
    tenantType?: 'CHAIN' | 'INDEPENDENT' | 'FRANCHISE' | 'MANAGEMENT_COMPANY' | 'VACATION_RENTAL';
    subscriptionPlan?: 'FREE' | 'STARTER' | 'PROFESSIONAL' | 'ENTERPRISE' | 'CUSTOM';
    isActive?: boolean;
    features?: {
      multiProperty?: boolean;
      channelManager?: boolean;
      advancedReporting?: boolean;
      paymentProcessing?: boolean;
      loyaltyProgram?: boolean;
    };
  }
): TenantContext {
  return {
    tenantId,
    tenantName: options?.tenantName || 'Test Tenant',
    tenantType: options?.tenantType || 'INDEPENDENT',
    subscriptionPlan: options?.subscriptionPlan || 'FREE',
    isActive: options?.isActive ?? true,
    features: {
      multiProperty: options?.features?.multiProperty ?? false,
      channelManager: options?.features?.channelManager ?? false,
      advancedReporting: options?.features?.advancedReporting ?? false,
      paymentProcessing: options?.features?.paymentProcessing ?? true,
      loyaltyProgram: options?.features?.loyaltyProgram ?? false,
    },
  };
}

// =============================================================================
// TENANT FILTER HELPERS
// =============================================================================

/**
 * Adds tenant filter to a database query object
 * @param query - The base query object
 * @param tenantId - The tenant ID to filter by
 * @returns Modified query object
 */
export function addTenantFilter<T extends Record<string, any>>(
  query: T,
  tenantId: string
): T & { tenantId: string } {
  return {
    ...query,
    tenantId,
  };
}

/**
 * Validates that all items in an array belong to the same tenant
 * @param items - Array of items with tenantId property
 * @param expectedTenantId - The expected tenant ID
 * @returns boolean - true if all items belong to the tenant
 * @throws Error if items belong to different tenants
 */
export function validateTenantConsistency(
  items: Array<{ tenantId: string }>,
  expectedTenantId: string
): boolean {
  const invalidItems = items.filter((item) => item.tenantId !== expectedTenantId);

  if (invalidItems.length > 0) {
    throw new Error(
      `Tenant consistency violation: Found ${invalidItems.length} items ` +
        `belonging to different tenants`
    );
  }

  return true;
}

// =============================================================================
// TYPE GUARDS
// =============================================================================

/**
 * Type guard to check if an object has a tenantId property
 */
export function hasTenantId(obj: any): obj is { tenantId: string } {
  return typeof obj === 'object' && obj !== null && typeof obj.tenantId === 'string';
}

/**
 * Type guard to check if an object has an optional tenantId property
 */
export function hasOptionalTenantId(obj: any): obj is { tenantId?: string } {
  return (
    typeof obj === 'object' &&
    obj !== null &&
    (obj.tenantId === undefined || typeof obj.tenantId === 'string')
  );
}
