import { z } from 'zod';
import {
  UUIDSchema,
  TimestampSchema,
  AuditFieldsSchema,
  SoftDeleteFieldsSchema,
  CurrencyCodeSchema,
  MoneyAmountSchema,
  DateSchema,
  TimeSchema,
  EmailSchema,
  PhoneSchema,
  AddressSchema,
} from './common';

// =============================================================================
// PROPERTY & ROOM SCHEMAS
// =============================================================================

// Property Group Schema
export const PropertyGroupSchema = z.object({
  id: UUIDSchema,
  name: z.string().min(1).max(255),
  shardKey: z.string().min(1).max(50),
  ...AuditFieldsSchema.shape,
});

// Property Schema
export const PropertySchema = z.object({
  id: UUIDSchema,
  propertyGroupId: UUIDSchema,
  code: z.string().min(1).max(50),
  name: z.string().min(1).max(255),
  brand: z.string().max(100).optional(),
  type: z.enum(['hotel', 'resort', 'apartment', 'villa', 'hostel']),

  // Address
  address: AddressSchema.optional(),
  timezone: z.string().default('UTC'),

  // Contact
  phone: PhoneSchema.optional(),
  email: EmailSchema.optional(),
  website: z.string().url().optional(),

  // Configuration
  checkInTime: TimeSchema.default('15:00:00'),
  checkOutTime: TimeSchema.default('11:00:00'),
  currencyCode: CurrencyCodeSchema.default('USD'),
  taxRate: z.number().min(0).max(1).default(0),
  serviceChargeRate: z.number().min(0).max(1).default(0),

  // Status
  status: z.enum(['active', 'inactive', 'maintenance']).default('active'),

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

// Room Type Schema
export const RoomTypeSchema = z.object({
  id: UUIDSchema,
  propertyId: UUIDSchema,
  code: z.string().min(1).max(50),
  name: z.string().min(1).max(255),
  description: z.string().optional(),
  category: z.string().max(50).optional(),

  // Specifications
  maxOccupancy: z.number().int().min(1).default(2),
  maxAdults: z.number().int().min(1).default(2),
  maxChildren: z.number().int().min(0).default(0),
  bedConfiguration: z.string().max(100).optional(),
  roomSizeSqm: z.number().positive().optional(),
  floorRange: z.string().max(50).optional(),

  // Pricing
  baseRate: MoneyAmountSchema.default(0),
  minRate: MoneyAmountSchema.default(0),
  maxRate: MoneyAmountSchema.default(9999.99),

  // Status
  status: z.enum(['active', 'inactive']).default('active'),

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

// Room Schema
export const RoomSchema = z.object({
  id: UUIDSchema,
  propertyId: UUIDSchema,
  roomTypeId: UUIDSchema,
  roomNumber: z.string().min(1).max(20),
  floor: z.number().int().optional(),

  // Features
  features: z.record(z.any()).default({}),
  maintenanceNotes: z.string().optional(),

  // Status
  status: z.enum(['available', 'occupied', 'maintenance', 'out_of_order', 'cleaning']).default('available'),

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

// Property-specific enums
export const PropertyTypeSchema = z.enum(['hotel', 'resort', 'apartment', 'villa', 'hostel']);
export const RoomStatusSchema = z.enum(['available', 'occupied', 'maintenance', 'out_of_order', 'cleaning']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type PropertyGroup = z.infer<typeof PropertyGroupSchema>;
export type Property = z.infer<typeof PropertySchema>;
export type RoomType = z.infer<typeof RoomTypeSchema>;
export type Room = z.infer<typeof RoomSchema>;

export type PropertyType = z.infer<typeof PropertyTypeSchema>;
export type RoomStatus = z.infer<typeof RoomStatusSchema>;
