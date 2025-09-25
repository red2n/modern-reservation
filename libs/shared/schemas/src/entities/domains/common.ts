import { z } from 'zod';

// =============================================================================
// COMMON/SHARED SCHEMAS
// =============================================================================

// UUID Schema
export const UUIDSchema = z.string().uuid('Invalid UUID format');

// Date/Time Schemas
export const DateSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Invalid date format (YYYY-MM-DD)');
export const TimeSchema = z.string().regex(/^\d{2}:\d{2}:\d{2}$/, 'Invalid time format (HH:MM:SS)');
export const TimestampSchema = z.string().datetime('Invalid datetime format');

// Currency and Money
export const CurrencyCodeSchema = z.string().length(3, 'Currency code must be 3 characters').toUpperCase();
export const MoneyAmountSchema = z.number().nonnegative('Amount must be non-negative').multipleOf(0.01);

// Contact Information
export const EmailSchema = z.string().email('Invalid email format').max(255);
export const PhoneSchema = z.string().regex(/^\+?[\d\s\-\(\)]{7,20}$/, 'Invalid phone number format');

// Address Schema
export const AddressSchema = z.object({
  line1: z.string().max(255).optional(),
  line2: z.string().max(255).optional(),
  city: z.string().max(100).optional(),
  state: z.string().max(100).optional(),
  country: z.string().length(2, 'Country code must be 2 characters').optional(),
  postalCode: z.string().max(20).optional(),
});

// Audit Fields Schema
export const AuditFieldsSchema = z.object({
  createdAt: TimestampSchema,
  updatedAt: TimestampSchema,
  createdBy: UUIDSchema.optional(),
  updatedBy: UUIDSchema.optional(),
});

// Soft Delete Fields Schema
export const SoftDeleteFieldsSchema = z.object({
  softDeleted: z.boolean().default(false),
  deletedAt: TimestampSchema.optional(),
  deletedBy: UUIDSchema.optional(),
  deletionReason: z.string().max(500).optional(),
  recoveryExpiresAt: TimestampSchema.optional(),
});

// Common Enums
export const StatusSchema = z.enum(['active', 'inactive', 'maintenance']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type Address = z.infer<typeof AddressSchema>;
export type AuditFields = z.infer<typeof AuditFieldsSchema>;
export type SoftDeleteFields = z.infer<typeof SoftDeleteFieldsSchema>;
export type Status = z.infer<typeof StatusSchema>;
