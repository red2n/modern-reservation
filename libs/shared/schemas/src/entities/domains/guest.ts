import { z } from 'zod';
import {
  AddressSchema,
  AuditFieldsSchema,
  DateSchema,
  EmailSchema,
  PhoneSchema,
  SoftDeleteFieldsSchema,
  TimestampSchema,
  UUIDSchema,
} from './common';

// =============================================================================
// GUEST SCHEMAS
// =============================================================================

export const GuestSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema.optional(), // Multi-tenancy: Optional - guests may be shared across tenants in a chain

  // Personal Information
  title: z.string().max(10).optional(),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  middleName: z.string().max(100).optional(),
  dateOfBirth: DateSchema.optional(),
  gender: z.enum(['male', 'female', 'other', 'prefer_not_to_say']).optional(),
  nationality: z.string().length(2).optional(),

  // Contact
  email: EmailSchema.optional(),
  phone: PhoneSchema.optional(),
  mobile: PhoneSchema.optional(),

  // Address
  address: AddressSchema.optional(),

  // Identity Documents (encrypted)
  passportNumber: z.string().max(50).optional(),
  passportCountry: z.string().length(2).optional(),
  passportExpiry: DateSchema.optional(),
  idCardNumber: z.string().max(50).optional(),
  idCardType: z.string().max(50).optional(),

  // Preferences
  preferences: z.record(z.any()).default({}),
  loyaltyNumber: z.string().max(50).optional(),
  loyaltyTier: z.string().max(20).optional(),
  vipStatus: z.boolean().default(false),

  // Communication Preferences
  marketingConsent: z.boolean().default(false),
  emailConsent: z.boolean().default(true),
  smsConsent: z.boolean().default(false),
  preferredLanguage: z.string().length(5).default('en'),

  // GDPR Compliance
  gdprConsent: z.boolean().default(false),
  gdprConsentDate: TimestampSchema.optional(),
  dataRetentionUntil: DateSchema.optional(),

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

// Guest-related enums
export const GenderSchema = z.enum(['male', 'female', 'other', 'prefer_not_to_say']);
export const LoyaltyTierSchema = z.enum(['bronze', 'silver', 'gold', 'platinum', 'diamond']);

// Guest Preference Categories
export const GuestPreferenceSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Preferences are tenant-specific
  guestId: UUIDSchema,
  category: z.enum(['room', 'dining', 'amenities', 'communication', 'accessibility']),
  preference: z.string().min(1).max(255),
  notes: z.string().optional(),
  priority: z.enum(['low', 'medium', 'high']).default('medium'),
  isActive: z.boolean().default(true),
  ...AuditFieldsSchema.shape,
});

// Guest Stay History
export const GuestStayHistorySchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Stay history is tenant-specific
  guestId: UUIDSchema,
  propertyId: UUIDSchema,
  reservationId: UUIDSchema,
  checkInDate: DateSchema,
  checkOutDate: DateSchema,
  roomType: z.string(),
  totalSpent: z.number().nonnegative(),
  satisfaction: z.number().min(1).max(5).optional(),
  notes: z.string().optional(),
  ...AuditFieldsSchema.shape,
});

// Guest Communication Log
export const GuestCommunicationSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Communication log is tenant-specific
  guestId: UUIDSchema,
  type: z.enum(['email', 'sms', 'phone', 'in_person', 'chat']),
  direction: z.enum(['inbound', 'outbound']),
  subject: z.string().max(255).optional(),
  content: z.string(),
  sentBy: UUIDSchema.optional(),
  sentAt: TimestampSchema,
  readAt: TimestampSchema.optional(),
  responseRequired: z.boolean().default(false),
  priority: z.enum(['low', 'medium', 'high', 'urgent']).default('medium'),
  ...AuditFieldsSchema.shape,
});

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type Guest = z.infer<typeof GuestSchema>;
export type Gender = z.infer<typeof GenderSchema>;
export type LoyaltyTier = z.infer<typeof LoyaltyTierSchema>;
export type GuestPreference = z.infer<typeof GuestPreferenceSchema>;
export type GuestStayHistory = z.infer<typeof GuestStayHistorySchema>;
export type GuestCommunication = z.infer<typeof GuestCommunicationSchema>;
