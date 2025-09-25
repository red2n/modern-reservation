import { z } from 'zod';
import {
  UUIDSchema,
  DateSchema,
  TimeSchema,
  TimestampSchema,
  AuditFieldsSchema,
  SoftDeleteFieldsSchema,
  MoneyAmountSchema,
  CurrencyCodeSchema,
} from './common';

// =============================================================================
// RESERVATION SCHEMAS
// =============================================================================

export const ReservationStatusSchema = z.enum([
  'inquiry',
  'tentative',
  'confirmed',
  'checked_in',
  'checked_out',
  'cancelled',
  'no_show',
  'walked_in'
]);

export const ReservationSchema = z.object({
  id: UUIDSchema,
  confirmationNumber: z.string().min(1).max(20),
  propertyId: UUIDSchema,

  // Guest Information
  primaryGuestId: UUIDSchema,
  guestCountAdults: z.number().int().min(1).default(1),
  guestCountChildren: z.number().int().min(0).default(0),
  guestCountInfants: z.number().int().min(0).default(0),

  // Stay Information
  checkInDate: DateSchema,
  checkOutDate: DateSchema,
  nights: z.number().int().min(1),

  // Room Information
  roomTypeId: UUIDSchema,
  roomCount: z.number().int().min(1).default(1),
  assignedRooms: z.array(UUIDSchema).default([]),

  // Pricing
  baseAmount: MoneyAmountSchema.default(0),
  taxesAmount: MoneyAmountSchema.default(0),
  feesAmount: MoneyAmountSchema.default(0),
  discountAmount: MoneyAmountSchema.default(0),
  totalAmount: MoneyAmountSchema.default(0),
  currencyCode: CurrencyCodeSchema.default('USD'),

  // Booking Information
  bookingSource: z.string().max(50).optional(),
  bookingChannel: z.string().max(50).optional(),
  ratePlanCode: z.string().max(50).optional(),
  packageCode: z.string().max(50).optional(),

  // Status
  status: ReservationStatusSchema.default('inquiry'),

  // Notes and Requests
  specialRequests: z.string().optional(),
  internalNotes: z.string().optional(),
  guestNotes: z.string().optional(),

  // Arrival/Departure
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

// Booking Channel Information
export const BookingChannelSchema = z.enum([
  'direct',
  'phone',
  'email',
  'walk_in',
  'booking_com',
  'expedia',
  'agoda',
  'hotels_com',
  'airbnb',
  'vrbo',
  'travel_agent',
  'corporate',
  'group'
]);

// Guest Type Enumeration
export const GuestTypeSchema = z.enum(['adult', 'child', 'infant']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type ReservationStatus = z.infer<typeof ReservationStatusSchema>;
export type Reservation = z.infer<typeof ReservationSchema>;
export type ReservationGuest = z.infer<typeof ReservationGuestSchema>;
export type ReservationRoom = z.infer<typeof ReservationRoomSchema>;
export type ReservationModification = z.infer<typeof ReservationModificationSchema>;
export type ReservationService = z.infer<typeof ReservationServiceSchema>;
export type BookingChannel = z.infer<typeof BookingChannelSchema>;
export type GuestType = z.infer<typeof GuestTypeSchema>;
