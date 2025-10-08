/**
 * Tenant Domain Schemas
 * Multi-tenancy support for hotel chains, franchises, and independent properties
 */

import { z } from "zod";
import { AuditFieldsSchema, SoftDeleteFieldsSchema, UUIDSchema } from "./common";

// =============================================================================
// ENUMS
// =============================================================================

/**
 * Tenant type classification
 */
export const TenantTypeSchema = z.enum([
  "CHAIN", // Hotel chain (e.g., Marriott, Hilton)
  "INDEPENDENT", // Independent hotel/property
  "FRANCHISE", // Franchised property
  "MANAGEMENT_COMPANY", // Property management company
  "VACATION_RENTAL", // Vacation rental company (Airbnb host, etc.)
]);

export type TenantType = z.infer<typeof TenantTypeSchema>;

/**
 * Subscription plan tiers
 */
export const SubscriptionPlanSchema = z.enum([
  "FREE", // Free tier (limited properties)
  "STARTER", // Small properties (1-5 properties)
  "PROFESSIONAL", // Medium properties (6-20 properties)
  "ENTERPRISE", // Large chains (unlimited properties)
  "CUSTOM", // Custom enterprise plan
]);

export type SubscriptionPlan = z.infer<typeof SubscriptionPlanSchema>;

/**
 * Tenant status
 */
export const TenantStatusSchema = z.enum([
  "ACTIVE", // Active and operational
  "SUSPENDED", // Temporarily suspended
  "TRIAL", // Trial period
  "EXPIRED", // Subscription expired
  "CANCELLED", // Account cancelled
]);

export type TenantStatus = z.infer<typeof TenantStatusSchema>;

// =============================================================================
// TENANT CONFIGURATION
// =============================================================================

/**
 * Tenant configuration and settings
 */
export const TenantConfigSchema = z.object({
  // Branding
  brandingEnabled: z.boolean().default(true),
  logoUrl: z.string().url().optional(),
  primaryColor: z.string().regex(/^#[0-9A-Fa-f]{6}$/).optional(),
  secondaryColor: z.string().regex(/^#[0-9A-Fa-f]{6}$/).optional(),

  // Features
  enableMultiProperty: z.boolean().default(true),
  enableChannelManager: z.boolean().default(false),
  enableAdvancedReporting: z.boolean().default(false),
  enablePaymentProcessing: z.boolean().default(true),
  enableLoyaltyProgram: z.boolean().default(false),

  // Limits
  maxProperties: z.number().int().positive().default(5),
  maxUsers: z.number().int().positive().default(10),
  maxReservationsPerMonth: z.number().int().positive().optional(),

  // Localization
  defaultCurrency: z.string().length(3).default("USD"),
  defaultLanguage: z.string().length(2).default("en"),
  defaultTimezone: z.string().default("America/New_York"),

  // Notifications
  enableEmailNotifications: z.boolean().default(true),
  enableSmsNotifications: z.boolean().default(false),
  enablePushNotifications: z.boolean().default(true),
});

export type TenantConfig = z.infer<typeof TenantConfigSchema>;

// =============================================================================
// SUBSCRIPTION
// =============================================================================

/**
 * Tenant subscription details
 */
export const TenantSubscriptionSchema = z.object({
  plan: SubscriptionPlanSchema,
  startDate: z.coerce.date(),
  endDate: z.coerce.date().optional(),
  trialEndsAt: z.coerce.date().optional(),
  autoRenew: z.boolean().default(true),
  paymentMethod: z.string().optional(), // Stripe payment method ID

  // Billing
  billingCycle: z.enum(["MONTHLY", "YEARLY", "CUSTOM"]).default("MONTHLY"),
  amount: z.number().nonnegative(),
  currency: z.string().length(3).default("USD"),
  nextBillingDate: z.coerce.date().optional(),

  // Status
  isTrial: z.boolean().default(false),
  isActive: z.boolean().default(true),
  cancelledAt: z.coerce.date().optional(),
});

export type TenantSubscription = z.infer<typeof TenantSubscriptionSchema>;

// =============================================================================
// MAIN TENANT SCHEMA
// =============================================================================

/**
 * Tenant (Organization) Schema
 * Represents a hotel chain, franchise, or independent property owner
 */
export const TenantSchema = z.object({
  id: UUIDSchema,

  // Basic Info
  name: z.string().min(1).max(200),
  slug: z.string().regex(/^[a-z0-9-]+$/), // URL-friendly identifier
  type: TenantTypeSchema,
  status: TenantStatusSchema.default("ACTIVE"),

  // Contact Info
  email: z.string().email(),
  phone: z.string().optional(),
  website: z.string().url().optional(),

  // Address
  addressLine1: z.string().optional(),
  addressLine2: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  country: z.string().length(2).optional(), // ISO 3166-1 alpha-2

  // Business Info
  taxId: z.string().optional(), // Tax identification number
  businessLicense: z.string().optional(),
  registrationNumber: z.string().optional(),

  // Configuration
  config: TenantConfigSchema.default({}),

  // Subscription
  subscription: TenantSubscriptionSchema,

  // Metadata
  metadata: z.record(z.string(), z.any()).optional(), // Custom fields

  // Timestamps & Audit
  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

export type Tenant = z.infer<typeof TenantSchema>;

// =============================================================================
// TENANT CONTEXT
// =============================================================================

/**
 * Tenant context for requests
 * Used in GraphQL context and API requests
 */
export const TenantContextSchema = z.object({
  tenantId: UUIDSchema,
  tenantName: z.string(),
  tenantType: TenantTypeSchema,
  subscriptionPlan: SubscriptionPlanSchema,
  isActive: z.boolean(),
  features: z.object({
    multiProperty: z.boolean(),
    channelManager: z.boolean(),
    advancedReporting: z.boolean(),
    paymentProcessing: z.boolean(),
    loyaltyProgram: z.boolean(),
  }),
});

export type TenantContext = z.infer<typeof TenantContextSchema>;

// =============================================================================
// TENANT CREATION & UPDATE
// =============================================================================

/**
 * Schema for creating a new tenant
 */
export const CreateTenantSchema = TenantSchema.omit({
  id: true,
  createdAt: true,
  updatedAt: true,
  createdBy: true,
  updatedBy: true,
  deletedAt: true,
  deletedBy: true,
}).extend({
  // Override to make fields required for creation
  name: z.string().min(2).max(200),
  email: z.string().email(),
  type: TenantTypeSchema,
});

export type CreateTenant = z.infer<typeof CreateTenantSchema>;

/**
 * Schema for updating tenant
 */
export const UpdateTenantSchema = TenantSchema.partial().omit({
  id: true,
  createdAt: true,
  createdBy: true,
  deletedAt: true,
  deletedBy: true,
});

export type UpdateTenant = z.infer<typeof UpdateTenantSchema>;

// =============================================================================
// TENANT FILTERS
// =============================================================================

/**
 * Filter schema for querying tenants
 */
export const TenantFilterSchema = z.object({
  id: UUIDSchema.optional(),
  slug: z.string().optional(),
  type: TenantTypeSchema.optional(),
  status: TenantStatusSchema.optional(),
  subscriptionPlan: SubscriptionPlanSchema.optional(),
  search: z.string().optional(), // Search by name, email, etc.
  isActive: z.boolean().optional(),
  includeDeleted: z.boolean().default(false),
});

export type TenantFilter = z.infer<typeof TenantFilterSchema>;
