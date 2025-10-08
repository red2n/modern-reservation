import { z } from 'zod';
import {
  AuditFieldsSchema,
  CurrencyCodeSchema,
  DateSchema,
  MoneyAmountSchema,
  SoftDeleteFieldsSchema,
  TimeSchema,
  TimestampSchema,
  UUIDSchema,
} from './common';

// =============================================================================
// RESERVATION SCHEMAS
// =============================================================================

// Reservation Status - Matches DB enum (UPPERCASE)
export const ReservationStatusSchema = z.enum([
  'PENDING',
  'CONFIRMED',
  'CHECKED_IN',
  'CHECKED_OUT',
  'CANCELLED',
  'NO_SHOW',
  'EXPIRED',
  'WAITLISTED',
  'MODIFIED',
  'ON_HOLD',
]);

// Reservation Source - Matches DB enum (UPPERCASE)
export const ReservationSourceSchema = z.enum([
  'DIRECT',
  'PHONE',
  'EMAIL',
  'WALK_IN',
  'BOOKING_COM',
  'EXPEDIA',
  'AIRBNB',
  'HOTELS_COM',
  'AGODA',
  'PRICELINE',
  'KAYAK',
  'TRIVAGO',
  'CORPORATE',
  'TRAVEL_AGENT',
  'GROUP_BOOKING',
  'EVENT_BOOKING',
  'MOBILE_APP',
  'SOCIAL_MEDIA',
  'API',
  'IMPORT',
  'ADMIN',
  'OTHER',
]);

export const ReservationSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Which tenant owns this reservation
  confirmationNumber: z.string().min(1).max(20),
  propertyId: UUIDSchema,

  // Guest Information (matching DB columns)
  primaryGuestId: UUIDSchema.optional(), // Can be null for walk-ins
  guestFirstName: z.string().min(1).max(100), // DB: guest_first_name
  guestLastName: z.string().min(1).max(100), // DB: guest_last_name
  guestEmail: z.string().email().max(255), // DB: guest_email
  guestPhone: z.string().max(20).optional(), // DB: guest_phone
  guestCountAdults: z.number().int().min(1).default(1),
  guestCountChildren: z.number().int().min(0).default(0),
  guestCountInfants: z.number().int().min(0).default(0),

  // Stay Information
  checkInDate: DateSchema,
  checkOutDate: DateSchema,
  nights: z.number().int().min(1),

  // Room Information (matching DB columns)
  roomTypeId: UUIDSchema.optional(),
  roomNumber: z.string().max(10).optional(), // DB: room_number
  roomCount: z.number().int().min(1).default(1),
  assignedRooms: z.array(UUIDSchema).default([]),

  // Pricing (matching DB columns)
  roomRate: MoneyAmountSchema, // DB: room_rate
  baseAmount: MoneyAmountSchema.default(0),
  taxes: MoneyAmountSchema.default(0).optional(), // DB: taxes
  fees: MoneyAmountSchema.default(0).optional(), // DB: fees
  taxesAmount: MoneyAmountSchema.default(0),
  feesAmount: MoneyAmountSchema.default(0),
  discountAmount: MoneyAmountSchema.default(0),
  totalAmount: MoneyAmountSchema, // DB: total_amount
  currency: CurrencyCodeSchema.default('USD'), // DB: currency (CHAR(3))
  currencyCode: CurrencyCodeSchema.default('USD'),

  // Booking Information (matching DB columns)
  bookingDate: TimestampSchema, // DB: booking_date
  source: ReservationSourceSchema, // DB: source (ENUM)
  bookingSource: ReservationSourceSchema.optional(),
  bookingChannel: z.string().max(50).optional(),
  ratePlanCode: z.string().max(50).optional(),
  packageCode: z.string().max(50).optional(),

  // Status (matching DB enum - UPPERCASE)
  status: ReservationStatusSchema.default('PENDING'),

  // Notes and Requests (matching DB columns)
  specialRequests: z.string().optional(), // DB: special_requests (TEXT)
  internalNotes: z.string().optional(), // DB: internal_notes (TEXT)
  guestNotes: z.string().optional(),

  // Arrival/Departure (matching DB columns)
  arrivalTime: z.string().max(5).optional(), // DB: arrival_time VARCHAR(5)
  departureTime: z.string().max(5).optional(), // DB: departure_time VARCHAR(5)
  estimatedArrivalTime: TimeSchema.optional(),
  actualArrivalTime: TimestampSchema.optional(),
  actualDepartureTime: TimestampSchema.optional(),

  // Cancellation
  cancelledAt: TimestampSchema.optional(),
  cancelledBy: UUIDSchema.optional(),
  cancellationReason: z.string().optional(),
  cancellationPolicyId: UUIDSchema.optional(),

  ...AuditFieldsSchema.shape,
  ...SoftDeleteFieldsSchema.shape,
});

// Reservation Guest Assignment
export const ReservationGuestSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Inherited from reservation
  reservationId: UUIDSchema,
  guestId: UUIDSchema,
  isPrimary: z.boolean().default(false),
  guestType: z.enum(['adult', 'child', 'infant']).default('adult'),
  roomAssignment: UUIDSchema.optional(),
  arrivalDate: DateSchema.optional(),
  departureDate: DateSchema.optional(),
  ...AuditFieldsSchema.shape,
});

// Reservation Room Assignment
export const ReservationRoomSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Inherited from reservation
  reservationId: UUIDSchema,
  roomId: UUIDSchema,
  checkInDate: DateSchema,
  checkOutDate: DateSchema,
  assignedAt: TimestampSchema,
  unassignedAt: TimestampSchema.optional(),
  guestCount: z.number().int().min(1).default(1),
  notes: z.string().optional(),
  ...AuditFieldsSchema.shape,
});

// Reservation Modification Log
export const ReservationModificationSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Inherited from reservation
  reservationId: UUIDSchema,
  modificationType: z.enum(['dates', 'guests', 'rooms', 'pricing', 'status', 'cancellation']),
  previousValue: z.record(z.any()),
  newValue: z.record(z.any()),
  reason: z.string().optional(),
  modifiedBy: UUIDSchema,
  modifiedAt: TimestampSchema,
  notes: z.string().optional(),
  ...AuditFieldsSchema.shape,
});

// Reservation Add-ons/Services
export const ReservationServiceSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema, // Multi-tenancy: Inherited from reservation
  reservationId: UUIDSchema,
  serviceType: z.enum(['spa', 'restaurant', 'transport', 'activity', 'equipment', 'other']),
  serviceName: z.string().min(1).max(255),
  serviceDate: DateSchema,
  serviceTime: TimeSchema.optional(),
  quantity: z.number().int().min(1).default(1),
  unitPrice: MoneyAmountSchema,
  totalPrice: MoneyAmountSchema,
  status: z.enum(['requested', 'confirmed', 'completed', 'cancelled']).default('requested'),
  notes: z.string().optional(),
  ...AuditFieldsSchema.shape,
});

// Guest Type Enumeration
export const GuestTypeSchema = z.enum(['adult', 'child', 'infant']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type ReservationStatus = z.infer<typeof ReservationStatusSchema>;
export type ReservationSource = z.infer<typeof ReservationSourceSchema>;
export type Reservation = z.infer<typeof ReservationSchema>;
export type ReservationGuest = z.infer<typeof ReservationGuestSchema>;
export type ReservationRoom = z.infer<typeof ReservationRoomSchema>;
export type ReservationModification = z.infer<typeof ReservationModificationSchema>;
export type ReservationService = z.infer<typeof ReservationServiceSchema>;
export type GuestType = z.infer<typeof GuestTypeSchema>;
