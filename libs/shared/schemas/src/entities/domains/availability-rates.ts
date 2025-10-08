import { z } from 'zod';
import {
  AuditFieldsSchema,
  DateSchema,
  MoneyAmountSchema,
  SoftDeleteFieldsSchema,
  UUIDSchema,
} from './common';

// =============================================================================
// RATE MANAGEMENT SCHEMAS
// =============================================================================

// Rate Status - Matches DB enum (UPPERCASE)
export const RateStatusSchema = z.enum(['DRAFT', 'ACTIVE', 'SUSPENDED', 'EXPIRED', 'ARCHIVED']);

// Rate Strategy - Matches DB enum (UPPERCASE)
export const RateStrategySchema = z.enum([
  'FIXED',
  'DYNAMIC',
  'SEASONAL',
  'LAST_MINUTE',
  'EARLY_BIRD',
  'LENGTH_OF_STAY',
  'OCCUPANCY_BASED',
  'REVENUE_OPTIMIZATION',
  'COMPETITIVE',
  'PROMOTIONAL',
]);

// Season Type - Matches DB enum (UPPERCASE)
export const SeasonTypeSchema = z.enum(['PEAK', 'HIGH', 'REGULAR', 'LOW', 'OFF_PEAK']);

// Availability Status - Matches DB enum (UPPERCASE)
export const AvailabilityStatusSchema = z.enum([
  'AVAILABLE',
  'OCCUPIED',
  'MAINTENANCE',
  'BLOCKED',
  'RESERVED',
]);

// Room Category - Matches DB enum (UPPERCASE)
export const RoomCategorySchema = z.enum([
  'STANDARD',
  'DELUXE',
  'SUITE',
  'EXECUTIVE',
  'PRESIDENTIAL',
]);

// Pricing Method - Matches DB enum (UPPERCASE)
export const PricingMethodSchema = z.enum(['FIXED', 'DYNAMIC', 'SEASONAL', 'OCCUPANCY_BASED']);

// Time Granularity - Matches DB enum (UPPERCASE)
export const TimeGranularitySchema = z.enum([
  'HOURLY',
  'DAILY',
  'WEEKLY',
  'MONTHLY',
  'QUARTERLY',
  'YEARLY',
]);

// Analytics Status - Matches DB enum (UPPERCASE)
export const AnalyticsStatusSchema = z.enum([
  'PENDING',
  'CALCULATING',
  'COMPLETED',
  'FAILED',
  'EXPIRED',
  'CACHED',
]);

// Metric Type - Matches DB enum (UPPERCASE)
export const MetricTypeSchema = z.enum([
  'OCCUPANCY_RATE',
  'ADR',
  'REVPAR',
  'TOTAL_REVENUE',
  'ROOM_REVENUE',
  'ANCILLARY_REVENUE',
  'BOOKING_COUNT',
  'CANCELLATION_RATE',
  'NO_SHOW_RATE',
  'AVERAGE_LENGTH_OF_STAY',
  'LEAD_TIME',
  'CUSTOMER_ACQUISITION_COST',
  'CUSTOMER_LIFETIME_VALUE',
  'CONVERSION_RATE',
  'CHANNEL_CONTRIBUTION',
  'MARKET_SEGMENT_MIX',
  'CUSTOM',
]);

export const RatePlanSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Rate plans are tenant-specific
  propertyId: UUIDSchema,
  code: z.string().min(1).max(50),
  name: z.string().min(1).max(255),
  description: z.string().optional(),

  // Configuration
  type: z.enum(['standard', 'corporate', 'package', 'promotional']),
  bookingWindowStart: z.number().int().optional(),
  bookingWindowEnd: z.number().int().optional(),
  minLengthOfStay: z.number().int().min(1).default(1),
  maxLengthOfStay: z.number().int().optional(),
  minAdvanceBooking: z.number().int().min(0).default(0),
  maxAdvanceBooking: z.number().int().optional(),

  // Restrictions
  blackoutDates: z.array(DateSchema).default([]),
  allowedDaysOfWeek: z.array(z.number().int().min(1).max(7)).optional(),

  // Pricing
  baseRate: MoneyAmountSchema.optional(),
  rateMultiplier: z.number().positive().default(1),
  isNetRate: z.boolean().default(false),
  commissionRate: z.number().min(0).max(1).default(0),

  // Policy
  cancellationPolicyId: UUIDSchema.optional(),

  // Validity
  status: RateStatusSchema.default('ACTIVE'),
  validFrom: DateSchema,
  validUntil: DateSchema,

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

export const DailyRateSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Daily rates are tenant-specific
  propertyId: UUIDSchema,
  roomTypeId: UUIDSchema,
  ratePlanId: UUIDSchema,
  stayDate: DateSchema,

  // Rate Information
  rate: MoneyAmountSchema,
  minLengthOfStay: z.number().int().min(1).default(1),
  maxLengthOfStay: z.number().int().optional(),
  closedToArrival: z.boolean().default(false),
  closedToDeparture: z.boolean().default(false),
  stopSell: z.boolean().default(false),

  // Availability
  availableRooms: z.number().int().min(0).optional(),
  soldRooms: z.number().int().min(0).default(0),
  blockedRooms: z.number().int().min(0).default(0),

  // Revenue Management
  revenueMultiplier: z.number().positive().default(1),
  demandFactor: z.number().positive().default(1),

  ...AuditFieldsSchema.shape,
});

// Seasonal Rate Configuration
export const SeasonalRateSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Seasonal rates are tenant-specific
  propertyId: UUIDSchema,
  name: z.string().min(1).max(255),
  startDate: DateSchema,
  endDate: DateSchema,
  rateAdjustment: z.number(), // Percentage adjustment
  adjustmentType: z.enum(['percentage', 'fixed_amount']),
  priority: z.number().int().min(1).default(1),
  isActive: z.boolean().default(true),
  description: z.string().optional(),
  ...AuditFieldsSchema.shape,
});

// Rate Restriction Rules
export const RateRestrictionSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Rate restrictions are tenant-specific
  propertyId: UUIDSchema,
  roomTypeId: UUIDSchema.optional(),
  ratePlanId: UUIDSchema.optional(),
  restrictionType: z.enum([
    'min_stay',
    'max_stay',
    'closed_arrival',
    'closed_departure',
    'stop_sell',
  ]),
  value: z.number().int().optional(),
  startDate: DateSchema,
  endDate: DateSchema,
  daysOfWeek: z.array(z.number().int().min(1).max(7)).optional(),
  reason: z.string().max(500).optional(),
  isActive: z.boolean().default(true),
  ...AuditFieldsSchema.shape,
});

// Dynamic Pricing Rules
export const DynamicPricingRuleSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Dynamic pricing rules are tenant-specific
  propertyId: UUIDSchema,
  name: z.string().min(1).max(255),
  description: z.string().optional(),

  // Trigger Conditions
  occupancyThreshold: z.number().min(0).max(1).optional(),
  daysFromArrival: z.number().int().min(0).optional(),
  demandLevel: z.enum(['low', 'medium', 'high']).optional(),
  competitorPricing: z.boolean().default(false),

  // Adjustment Rules
  adjustmentType: z.enum(['percentage', 'fixed_amount']),
  adjustmentValue: z.number(),
  minRate: MoneyAmountSchema.optional(),
  maxRate: MoneyAmountSchema.optional(),

  // Application
  roomTypeIds: z.array(UUIDSchema),
  ratePlanIds: z.array(UUIDSchema),
  priority: z.number().int().min(1).default(1),
  isActive: z.boolean().default(true),

  ...AuditFieldsSchema.shape,
});

// Rate Calendar
export const RateCalendarSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Rate calendar is tenant-specific
  propertyId: UUIDSchema,
  roomTypeId: UUIDSchema,
  ratePlanId: UUIDSchema,

  // Date Range
  startDate: DateSchema,
  endDate: DateSchema,

  // Rates by Date
  rates: z.record(MoneyAmountSchema), // Date string as key, rate as value
  restrictions: z.record(
    z.object({
      minStay: z.number().int().optional(),
      maxStay: z.number().int().optional(),
      closedArrival: z.boolean().default(false),
      closedDeparture: z.boolean().default(false),
      stopSell: z.boolean().default(false),
    })
  ),

  // Availability by Date
  availability: z.record(z.number().int().min(0)), // Date string as key, available rooms as value

  ...AuditFieldsSchema.shape,
});

// Package Rates
export const PackageRateSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Package rates are tenant-specific
  propertyId: UUIDSchema,
  packageCode: z.string().min(1).max(50),
  packageName: z.string().min(1).max(255),
  description: z.string().optional(),

  // Package Components
  includesAccommodation: z.boolean().default(true),
  includedServices: z.array(z.string()),
  additionalCharges: z.array(
    z.object({
      serviceName: z.string(),
      amount: MoneyAmountSchema,
      isOptional: z.boolean().default(false),
    })
  ),

  // Pricing
  basePackageRate: MoneyAmountSchema,
  accommodationRate: MoneyAmountSchema.optional(),
  serviceCharges: MoneyAmountSchema.default(0),
  totalPackageRate: MoneyAmountSchema,

  // Validity
  validFrom: DateSchema,
  validUntil: DateSchema,
  minLengthOfStay: z.number().int().min(1).default(1),
  maxLengthOfStay: z.number().int().optional(),

  // Booking Rules
  advanceBookingRequired: z.number().int().min(0).default(0),
  cancellationPolicyId: UUIDSchema.optional(),
  isRefundable: z.boolean().default(true),

  status: RateStatusSchema.default('ACTIVE'),
  ...AuditFieldsSchema.shape,
});

// Rate Plan Types
export const RatePlanTypeSchema = z.enum(['standard', 'corporate', 'package', 'promotional']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

// Enum Types
export type RateStatus = z.infer<typeof RateStatusSchema>;
export type RateStrategy = z.infer<typeof RateStrategySchema>;
export type SeasonType = z.infer<typeof SeasonTypeSchema>;
export type AvailabilityStatus = z.infer<typeof AvailabilityStatusSchema>;
export type RoomCategory = z.infer<typeof RoomCategorySchema>;
export type PricingMethod = z.infer<typeof PricingMethodSchema>;
export type TimeGranularity = z.infer<typeof TimeGranularitySchema>;
export type AnalyticsStatus = z.infer<typeof AnalyticsStatusSchema>;
export type MetricType = z.infer<typeof MetricTypeSchema>;

// Entity Types
export type RatePlan = z.infer<typeof RatePlanSchema>;
export type DailyRate = z.infer<typeof DailyRateSchema>;
export type SeasonalRate = z.infer<typeof SeasonalRateSchema>;
export type RateRestriction = z.infer<typeof RateRestrictionSchema>;
export type DynamicPricingRule = z.infer<typeof DynamicPricingRuleSchema>;
export type RateCalendar = z.infer<typeof RateCalendarSchema>;
export type PackageRate = z.infer<typeof PackageRateSchema>;
export type RatePlanType = z.infer<typeof RatePlanTypeSchema>;
