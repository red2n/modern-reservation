// =============================================================================
// MODULAR ZOD SCHEMAS - DOMAIN INDEX
// =============================================================================
// This file exports all domain-specific Zod schemas from individual modules
// for improved development workflow and maintainability

// Common/Shared Schemas
export * from './domains/common';

// Domain-Specific Schemas
export * from './domains/property';
export * from './domains/guest';
export * from './domains/reservation';
export * from './domains/payment';
export * from './domains/availability-rates';
export * from './domains/user';

// =============================================================================
// CONSOLIDATED SCHEMA COLLECTIONS
// =============================================================================

import {
  // Common Schemas
  UUIDSchema,
  DateSchema,
  TimeSchema,
  TimestampSchema,
  CurrencyCodeSchema,
  MoneyAmountSchema,
  EmailSchema,
  PhoneSchema,
  AddressSchema,
  AuditFieldsSchema,
  SoftDeleteFieldsSchema,
  StatusSchema,
} from './domains/common';

import {
  PropertyGroupSchema,
  PropertySchema,
  RoomTypeSchema,
  RoomSchema,
  PropertyTypeSchema,
  RoomStatusSchema,
} from './domains/property';

import {
  GuestSchema,
  GenderSchema,
  LoyaltyTierSchema,
  GuestPreferenceSchema,
  GuestStayHistorySchema,
  GuestCommunicationSchema,
} from './domains/guest';

import {
  ReservationStatusSchema,
  ReservationSchema,
  ReservationGuestSchema,
  ReservationRoomSchema,
  ReservationModificationSchema,
  ReservationServiceSchema,
  BookingChannelSchema,
  GuestTypeSchema,
} from './domains/reservation';

import {
  PaymentStatusSchema,
  PaymentMethodSchema,
  PaymentTransactionSchema,
  PaymentGatewaySchema,
  PaymentPlanSchema,
  PaymentScheduleSchema,
  RefundRequestSchema,
  PaymentMethodTypeSchema,
  TransactionTypeSchema,
} from './domains/payment';

import {
  RatePlanSchema,
  DailyRateSchema,
  SeasonalRateSchema,
  RateRestrictionSchema,
  DynamicPricingRuleSchema,
  RateCalendarSchema,
  PackageRateSchema,
  RatePlanTypeSchema,
} from './domains/availability-rates';

import {
  UserSchema,
  RoleSchema,
  UserRoleSchema,
  PermissionSchema,
  UserSessionSchema,
  PasswordResetTokenSchema,
  EmailVerificationTokenSchema,
  UserAuditLogSchema,
  UserPreferencesSchema,
  UserStatusSchema,
  DeviceTypeSchema,
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
  Reservation: ReservationSchema,
  ReservationGuest: ReservationGuestSchema,
  ReservationRoom: ReservationRoomSchema,
  ReservationModification: ReservationModificationSchema,
  ReservationService: ReservationServiceSchema,
  BookingChannel: BookingChannelSchema,
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
  common: (data: unknown) => CommonSchemas,
  property: (data: unknown) => PropertySchemas,
  guest: (data: unknown) => GuestSchemas,
  reservation: (data: unknown) => ReservationSchemas,
  payment: (data: unknown) => PaymentSchemas,
  availabilityRates: (data: unknown) => AvailabilityRateSchemas,
  user: (data: unknown) => UserSchemas,
};

/**
 * Get all schemas for a specific domain
 */
export const getDomainSchemas = (domain: keyof typeof validateDomain) => {
  switch (domain) {
    case 'common': return CommonSchemas;
    case 'property': return PropertySchemas;
    case 'guest': return GuestSchemas;
    case 'reservation': return ReservationSchemas;
    case 'payment': return PaymentSchemas;
    case 'availabilityRates': return AvailabilityRateSchemas;
    case 'user': return UserSchemas;
    default: throw new Error(`Unknown domain: ${domain}`);
  }
};
