/**
 * Central schemas export - imports from shared schemas package
 * Single source of truth for all entity schemas and types
 */

import type { z } from 'zod';

// Import all entity schemas from shared package
export {
  // Common
  AddressSchema,
  AuditFieldsSchema,
  CurrencyCodeSchema,
  DateSchema,
  EmailSchema,
  GenderSchema,
  GuestPreferenceSchema,
  // Guest
  GuestSchema,
  LoyaltyTierSchema,
  MoneyAmountSchema,
  // Payment
  PaymentSchemas,
  PhoneSchema,
  PropertyGroupSchema,
  // Property
  PropertySchema,
  PropertyTypeSchema,
  ReservationGuestSchema,
  ReservationRoomSchema,
  // Reservation
  ReservationSchema,
  ReservationStatusSchema,
  RoomSchema,
  RoomStatusSchema,
  RoomTypeSchema,
  SoftDeleteFieldsSchema,
  TimeSchema,
  TimestampSchema,
  UUIDSchema,
} from '@modern-reservation/schemas';

// Re-export Zod for convenience
export { z } from 'zod';

// Helper to infer types from schemas
export type InferSchema<T extends z.ZodType> = z.infer<T>;
