// =============================================================================
// MODULAR ZOD SCHEMAS - DOMAIN INDEX
// =============================================================================
// This file exports all domain-specific Zod schemas from individual modules
// for improved development workflow and maintainability

export * from './domains/availability-rates';
// Common/Shared Schemas
export * from './domains/common';
export * from './domains/guest';
export * from './domains/payment';
export * from './domains/property';
export * from './domains/reservation';
// Domain-Specific Schemas
export * from './domains/tenant';
export * from './domains/user';

// =============================================================================
// CONSOLIDATED SCHEMA COLLECTIONS
// =============================================================================

import {
  AnalyticsStatusSchema,
  AvailabilityStatusSchema,
  DailyRateSchema,
  DynamicPricingRuleSchema,
  MetricTypeSchema,
  PackageRateSchema,
  PricingMethodSchema,
  RateCalendarSchema,
  RatePlanSchema,
  RatePlanTypeSchema,
  RateRestrictionSchema,
  RateStatusSchema,
  RateStrategySchema,
  RoomCategorySchema,
  SeasonalRateSchema,
  SeasonTypeSchema,
  TimeGranularitySchema,
} from './domains/availability-rates';
import {
  AddressSchema,
  AuditFieldsSchema,
  CurrencyCodeSchema,
  DateSchema,
  EmailSchema,
  MoneyAmountSchema,
  PhoneSchema,
  SoftDeleteFieldsSchema,
  StatusSchema,
  TimeSchema,
  TimestampSchema,
  // Common Schemas
  UUIDSchema,
} from './domains/common';

import {
  GenderSchema,
  GuestCommunicationSchema,
  GuestPreferenceSchema,
  GuestSchema,
  GuestStayHistorySchema,
  LoyaltyTierSchema,
} from './domains/guest';
import {
  PaymentGatewaySchema,
  PaymentMethodSchema,
  PaymentMethodTypeSchema,
  PaymentPlanSchema,
  PaymentScheduleSchema,
  PaymentStatusSchema,
  PaymentTransactionSchema,
  RefundRequestSchema,
  TransactionTypeSchema,
} from './domains/payment';
import {
  PropertyGroupSchema,
  PropertySchema,
  PropertyTypeSchema,
  RoomSchema,
  RoomStatusSchema,
  RoomTypeSchema,
} from './domains/property';
import {
  GuestTypeSchema,
  ReservationGuestSchema,
  ReservationModificationSchema,
  ReservationRoomSchema,
  ReservationSchema,
  ReservationServiceSchema,
  ReservationSourceSchema,
  ReservationStatusSchema,
} from './domains/reservation';

import {
  DeviceTypeSchema,
  EmailVerificationTokenSchema,
  PasswordResetTokenSchema,
  PermissionSchema,
  RoleSchema,
  UserAuditLogSchema,
  UserPreferencesSchema,
  UserRoleSchema,
  UserSchema,
  UserSessionSchema,
  UserStatusSchema,
} from './domains/user';

// =============================================================================
// ORGANIZED SCHEMA COLLECTIONS BY DOMAIN
// =============================================================================

export const CommonSchemas = {
  UUID: UUIDSchema,
  Date: DateSchema,
  Time: TimeSchema,
  Timestamp: TimestampSchema,
  CurrencyCode: CurrencyCodeSchema,
  MoneyAmount: MoneyAmountSchema,
  Email: EmailSchema,
  Phone: PhoneSchema,
  Address: AddressSchema,
  AuditFields: AuditFieldsSchema,
  SoftDeleteFields: SoftDeleteFieldsSchema,
  Status: StatusSchema,
};

export const PropertySchemas = {
  PropertyGroup: PropertyGroupSchema,
  Property: PropertySchema,
  RoomType: RoomTypeSchema,
  Room: RoomSchema,
  PropertyType: PropertyTypeSchema,
  RoomStatus: RoomStatusSchema,
};

export const GuestSchemas = {
  Guest: GuestSchema,
  Gender: GenderSchema,
  LoyaltyTier: LoyaltyTierSchema,
  GuestPreference: GuestPreferenceSchema,
  GuestStayHistory: GuestStayHistorySchema,
  GuestCommunication: GuestCommunicationSchema,
};

export const ReservationSchemas = {
  ReservationStatus: ReservationStatusSchema,
  ReservationSource: ReservationSourceSchema,
  Reservation: ReservationSchema,
  ReservationGuest: ReservationGuestSchema,
  ReservationRoom: ReservationRoomSchema,
  ReservationModification: ReservationModificationSchema,
  ReservationService: ReservationServiceSchema,
  GuestType: GuestTypeSchema,
};

export const PaymentSchemas = {
  PaymentStatus: PaymentStatusSchema,
  PaymentMethod: PaymentMethodSchema,
  PaymentTransaction: PaymentTransactionSchema,
  PaymentGateway: PaymentGatewaySchema,
  PaymentPlan: PaymentPlanSchema,
  PaymentSchedule: PaymentScheduleSchema,
  RefundRequest: RefundRequestSchema,
  PaymentMethodType: PaymentMethodTypeSchema,
  TransactionType: TransactionTypeSchema,
};

export const AvailabilityRateSchemas = {
  RatePlan: RatePlanSchema,
  DailyRate: DailyRateSchema,
  SeasonalRate: SeasonalRateSchema,
  RateRestriction: RateRestrictionSchema,
  DynamicPricingRule: DynamicPricingRuleSchema,
  RateCalendar: RateCalendarSchema,
  PackageRate: PackageRateSchema,
  RatePlanType: RatePlanTypeSchema,
  RateStatus: RateStatusSchema,
  RateStrategy: RateStrategySchema,
  SeasonType: SeasonTypeSchema,
  AvailabilityStatus: AvailabilityStatusSchema,
  RoomCategory: RoomCategorySchema,
  PricingMethod: PricingMethodSchema,
  TimeGranularity: TimeGranularitySchema,
  AnalyticsStatus: AnalyticsStatusSchema,
  MetricType: MetricTypeSchema,
};

export const UserSchemas = {
  User: UserSchema,
  Role: RoleSchema,
  UserRole: UserRoleSchema,
  Permission: PermissionSchema,
  UserSession: UserSessionSchema,
  PasswordResetToken: PasswordResetTokenSchema,
  EmailVerificationToken: EmailVerificationTokenSchema,
  UserAuditLog: UserAuditLogSchema,
  UserPreferences: UserPreferencesSchema,
  UserStatus: UserStatusSchema,
  DeviceType: DeviceTypeSchema,
};

// =============================================================================
// CONSOLIDATED ENTITY SCHEMAS (BACKWARD COMPATIBILITY)
// =============================================================================
// Maintains compatibility with existing code while providing modular structure

export const EntitySchemas = {
  // Core Entities
  PropertyGroup: PropertyGroupSchema,
  Property: PropertySchema,
  RoomType: RoomTypeSchema,
  Room: RoomSchema,
  Guest: GuestSchema,
  Reservation: ReservationSchema,
  User: UserSchema,

  // Rate Management
  RatePlan: RatePlanSchema,
  DailyRate: DailyRateSchema,

  // Payment
  PaymentMethod: PaymentMethodSchema,
  PaymentTransaction: PaymentTransactionSchema,

  // Supporting Entities
  Role: RoleSchema,
  Permission: PermissionSchema,
  UserSession: UserSessionSchema,
  GuestPreference: GuestPreferenceSchema,
  ReservationGuest: ReservationGuestSchema,
  PaymentGateway: PaymentGatewaySchema,

  // Common
  Address: AddressSchema,
  AuditFields: AuditFieldsSchema,
  SoftDeleteFields: SoftDeleteFieldsSchema,
};

// =============================================================================
// DOMAIN VALIDATION UTILITIES
// =============================================================================

/**
 * Validates data against a specific domain schema collection
 */
export const validateDomain = {
  common: (_data: unknown) => CommonSchemas,
  property: (_data: unknown) => PropertySchemas,
  guest: (_data: unknown) => GuestSchemas,
  reservation: (_data: unknown) => ReservationSchemas,
  payment: (_data: unknown) => PaymentSchemas,
  availabilityRates: (_data: unknown) => AvailabilityRateSchemas,
  user: (_data: unknown) => UserSchemas,
};

/**
 * Get all schemas for a specific domain
 */
export const getDomainSchemas = (domain: keyof typeof validateDomain) => {
  switch (domain) {
    case 'common':
      return CommonSchemas;
    case 'property':
      return PropertySchemas;
    case 'guest':
      return GuestSchemas;
    case 'reservation':
      return ReservationSchemas;
    case 'payment':
      return PaymentSchemas;
    case 'availabilityRates':
      return AvailabilityRateSchemas;
    case 'user':
      return UserSchemas;
    default:
      throw new Error(`Unknown domain: ${domain}`);
  }
};
