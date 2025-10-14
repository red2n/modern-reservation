import { z } from 'zod';
import {
  GuestSchema,
  PaymentSchemas,
  PropertySchema,
  ReservationSchema,
  RoomSchema,
} from '../entities';

// Re-export authentication schemas
export * from './auth';

// Extract the main schema from PaymentSchemas
const { PaymentTransaction: PaymentSchema } = PaymentSchemas;

// Common API Response Schemas
export const ApiResponseSchema = z.object({
  success: z.boolean(),
  message: z.string().optional(),
  timestamp: z.string().datetime(),
  requestId: z.string().uuid(),
});

export const PaginationSchema = z.object({
  page: z.number().int().positive().default(1),
  limit: z.number().int().positive().max(100).default(20),
  total: z.number().int().nonnegative(),
  totalPages: z.number().int().nonnegative(),
  hasNext: z.boolean(),
  hasPrev: z.boolean(),
});

export const SortSchema = z.object({
  field: z.string(),
  direction: z.enum(['asc', 'desc']).default('asc'),
});

export const FilterSchema = z.object({
  field: z.string(),
  operator: z.enum([
    'eq',
    'ne',
    'gt',
    'gte',
    'lt',
    'lte',
    'in',
    'nin',
    'contains',
    'startsWith',
    'endsWith',
  ]),
  value: z.union([z.string(), z.number(), z.boolean(), z.array(z.any())]),
});

// Generic Paginated Response
export const PaginatedResponseSchema = <T extends z.ZodType>(itemSchema: T) =>
  ApiResponseSchema.extend({
    data: z.object({
      items: z.array(itemSchema),
      pagination: PaginationSchema,
    }),
  });

// Property API Schemas
export const CreatePropertyRequestSchema = z.object({
  name: z.string().min(1).max(255),
  description: z.string().max(1000).optional(),
  propertyType: z.enum(['hotel', 'resort', 'apartment', 'villa', 'hostel']),
  address: z.string().min(1).max(500),
  city: z.string().min(1).max(100),
  state: z.string().max(100).optional(),
  country: z.string().min(1).max(100),
  postalCode: z.string().max(20).optional(),
  timezone: z.string(),
  phone: z.string().optional(),
  email: z.string().email().optional(),
  website: z.string().url().optional(),
  checkInTime: z
    .string()
    .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)
    .default('15:00'),
  checkOutTime: z
    .string()
    .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/)
    .default('11:00'),
  currency: z.string().length(3).default('USD'),
  taxRate: z.number().min(0).max(1).default(0),
  amenities: z.array(z.string()).default([]),
  policies: z.record(z.string()).default({}),
  isActive: z.boolean().default(true),
});

export const UpdatePropertyRequestSchema = CreatePropertyRequestSchema.partial();

export const PropertyResponseSchema = ApiResponseSchema.extend({
  data: PropertySchema,
});

export const PropertiesResponseSchema = PaginatedResponseSchema(PropertySchema);

// Room API Schemas
export const CreateRoomRequestSchema = z.object({
  propertyId: z.string().uuid(),
  roomNumber: z.string().min(1).max(20),
  roomType: z.string().min(1).max(100),
  floor: z.number().int().optional(),
  capacity: z.number().int().positive(),
  basePrice: z.number().positive(),
  currency: z.string().length(3).default('USD'),
  size: z.number().positive().optional(),
  bedType: z.string().optional(),
  bedCount: z.number().int().positive().default(1),
  amenities: z.array(z.string()).default([]),
  description: z.string().max(1000).optional(),
  images: z.array(z.string().url()).default([]),
  isActive: z.boolean().default(true),
});

export const UpdateRoomRequestSchema = CreateRoomRequestSchema.partial().omit({ propertyId: true });

export const RoomResponseSchema = ApiResponseSchema.extend({
  data: RoomSchema,
});

export const RoomsResponseSchema = PaginatedResponseSchema(RoomSchema);

// Room Search Schema
export const RoomSearchRequestSchema = z.object({
  propertyId: z.string().uuid().optional(),
  checkInDate: z.string().date(),
  checkOutDate: z.string().date(),
  adults: z.number().int().positive(),
  children: z.number().int().nonnegative().default(0),
  roomType: z.string().optional(),
  minPrice: z.number().positive().optional(),
  maxPrice: z.number().positive().optional(),
  amenities: z.array(z.string()).optional(),
  sortBy: z.enum(['price', 'capacity', 'rating', 'roomNumber']).default('price'),
  sortOrder: z.enum(['asc', 'desc']).default('asc'),
});

export const AvailableRoomSchema = RoomSchema.extend({
  availableDate: z.string().date(),
  currentPrice: z.number().positive(),
  isAvailable: z.boolean(),
  restrictions: z
    .object({
      minStay: z.number().int().positive().optional(),
      maxStay: z.number().int().positive().optional(),
      closedToArrival: z.boolean().default(false),
      closedToDeparture: z.boolean().default(false),
    })
    .optional(),
});

export const RoomSearchResponseSchema = ApiResponseSchema.extend({
  data: z.object({
    availableRooms: z.array(AvailableRoomSchema),
    searchCriteria: RoomSearchRequestSchema,
    totalFound: z.number().int().nonnegative(),
  }),
});

// Guest API Schemas
export const CreateGuestRequestSchema = z.object({
  email: z.string().email(),
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  phone: z.string().optional(),
  dateOfBirth: z.string().date().optional(),
  nationality: z.string().max(50).optional(),
  passportNumber: z.string().max(50).optional(),
  loyaltyNumber: z.string().max(50).optional(),
  preferences: z.record(z.string()).default({}),
  emergencyContact: z
    .object({
      name: z.string(),
      phone: z.string(),
      relationship: z.string(),
    })
    .optional(),
  address: z
    .object({
      street: z.string(),
      city: z.string(),
      state: z.string().optional(),
      country: z.string(),
      postalCode: z.string().optional(),
    })
    .optional(),
});

export const UpdateGuestRequestSchema = CreateGuestRequestSchema.partial();

export const GuestResponseSchema = ApiResponseSchema.extend({
  data: GuestSchema,
});

export const GuestsResponseSchema = PaginatedResponseSchema(GuestSchema);

// Reservation API Schemas
export const CreateReservationRequestSchema = z.object({
  propertyId: z.string().uuid(),
  roomId: z.string().uuid(),
  guestId: z.string().uuid(),
  checkInDate: z.string().date(),
  checkOutDate: z.string().date(),
  adults: z.number().int().positive(),
  children: z.number().int().nonnegative().default(0),
  source: z
    .enum(['direct', 'booking_dot_com', 'expedia', 'airbnb', 'phone', 'walk_in'])
    .default('direct'),
  rateCode: z.string().optional(),
  specialRequests: z.string().max(1000).optional(),
  guestNotes: z.string().max(1000).optional(),
  corporateCode: z.string().optional(),
  groupCode: z.string().optional(),
  marketSegment: z.string().optional(),
  additionalGuests: z
    .array(
      z.object({
        firstName: z.string(),
        lastName: z.string(),
        age: z.number().int().nonnegative().optional(),
      })
    )
    .default([]),
});

export const UpdateReservationRequestSchema = z.object({
  checkInDate: z.string().date().optional(),
  checkOutDate: z.string().date().optional(),
  adults: z.number().int().positive().optional(),
  children: z.number().int().nonnegative().optional(),
  specialRequests: z.string().max(1000).optional(),
  guestNotes: z.string().max(1000).optional(),
  additionalGuests: z
    .array(
      z.object({
        firstName: z.string(),
        lastName: z.string(),
        age: z.number().int().nonnegative().optional(),
      })
    )
    .optional(),
});

export const ReservationResponseSchema = ApiResponseSchema.extend({
  data: ReservationSchema,
});

export const ReservationsResponseSchema = PaginatedResponseSchema(ReservationSchema);

// Check-in/Check-out Schemas
export const CheckInRequestSchema = z.object({
  reservationId: z.string().uuid(),
  actualArrivalTime: z.string().datetime().optional(),
  roomKeys: z.array(z.string()).optional(),
  depositAmount: z.number().nonnegative().optional(),
  identificationVerified: z.boolean().default(false),
  signatureObtained: z.boolean().default(false),
  specialInstructions: z.string().max(500).optional(),
});

export const CheckOutRequestSchema = z.object({
  reservationId: z.string().uuid(),
  actualDepartureTime: z.string().datetime().optional(),
  roomCondition: z
    .enum(['good', 'needs_cleaning', 'maintenance_required', 'damaged'])
    .default('good'),
  additionalCharges: z
    .array(
      z.object({
        description: z.string(),
        amount: z.number().positive(),
        category: z.string(),
      })
    )
    .default([]),
  guestSatisfactionScore: z.number().min(1).max(5).optional(),
  feedback: z.string().max(1000).optional(),
});

// Payment API Schemas
export const CreatePaymentRequestSchema = z.object({
  reservationId: z.string().uuid(),
  amount: z.number().positive(),
  currency: z.string().length(3),
  paymentMethod: z.enum(['credit_card', 'debit_card', 'cash', 'bank_transfer', 'digital_wallet']),
  paymentDetails: z
    .object({
      cardNumber: z.string().optional(),
      expiryMonth: z.number().int().min(1).max(12).optional(),
      expiryYear: z.number().int().optional(),
      cvv: z.string().optional(),
      cardHolderName: z.string().optional(),
      billingAddress: z
        .object({
          street: z.string(),
          city: z.string(),
          state: z.string().optional(),
          country: z.string(),
          postalCode: z.string().optional(),
        })
        .optional(),
    })
    .optional(),
  description: z.string().optional(),
  reference: z.string().optional(),
});

export const PaymentResponseSchema = ApiResponseSchema.extend({
  data: PaymentSchema,
});

export const PaymentsResponseSchema = PaginatedResponseSchema(PaymentSchema);

// Refund Schema
export const CreateRefundRequestSchema = z.object({
  paymentId: z.string().uuid(),
  amount: z.number().positive(),
  reason: z.string().min(1).max(500),
  refundMethod: z
    .enum(['original_payment_method', 'bank_transfer', 'cash'])
    .default('original_payment_method'),
});

// Availability API Schemas
export const AvailabilityRequestSchema = z.object({
  propertyId: z.string().uuid(),
  roomId: z.string().uuid().optional(),
  startDate: z.string().date(),
  endDate: z.string().date(),
});

export const AvailabilitySchema = z.object({
  roomId: z.string().uuid(),
  date: z.string().date(),
  isAvailable: z.boolean(),
  price: z.number().positive(),
  currency: z.string().length(3),
  minStay: z.number().int().positive().optional(),
  maxStay: z.number().int().positive().optional(),
  closedToArrival: z.boolean(),
  closedToDeparture: z.boolean(),
  availableUnits: z.number().int().nonnegative(),
});

export const AvailabilityResponseSchema = ApiResponseSchema.extend({
  data: z.object({
    availability: z.array(AvailabilitySchema),
    propertyId: z.string().uuid(),
    dateRange: z.object({
      startDate: z.string().date(),
      endDate: z.string().date(),
    }),
  }),
});

export const UpdateAvailabilityRequestSchema = z.object({
  propertyId: z.string().uuid(),
  roomId: z.string().uuid(),
  updates: z.array(
    z.object({
      date: z.string().date(),
      isAvailable: z.boolean().optional(),
      price: z.number().positive().optional(),
      minStay: z.number().int().positive().optional(),
      maxStay: z.number().int().positive().optional(),
      closedToArrival: z.boolean().optional(),
      closedToDeparture: z.boolean().optional(),
    })
  ),
});

// Rate Management Schemas
export const RatePlanSchema = z.object({
  id: z.string().uuid(),
  propertyId: z.string().uuid(),
  name: z.string(),
  description: z.string().optional(),
  ratePlanCode: z.string(),
  isActive: z.boolean(),
  cancellationPolicy: z.string(),
  advanceBookingDays: z.number().int().nonnegative().optional(),
  minStay: z.number().int().positive().optional(),
  maxStay: z.number().int().positive().optional(),
  blackoutDates: z.array(z.string().date()).optional(),
  discountPercent: z.number().min(0).max(100).optional(),
  createdAt: z.string().datetime(),
  updatedAt: z.string().datetime(),
});

export const CreateRatePlanRequestSchema = z.object({
  propertyId: z.string().uuid(),
  name: z.string().min(1).max(255),
  description: z.string().max(1000).optional(),
  ratePlanCode: z.string().min(1).max(50),
  cancellationPolicy: z.string().min(1),
  advanceBookingDays: z.number().int().nonnegative().optional(),
  minStay: z.number().int().positive().optional(),
  maxStay: z.number().int().positive().optional(),
  blackoutDates: z.array(z.string().date()).default([]),
  discountPercent: z.number().min(0).max(100).default(0),
  isActive: z.boolean().default(true),
});

// Analytics and Reporting Schemas
export const OccupancyReportRequestSchema = z.object({
  propertyId: z.string().uuid(),
  startDate: z.string().date(),
  endDate: z.string().date(),
  groupBy: z.enum(['day', 'week', 'month']).default('day'),
  roomType: z.string().optional(),
});

export const RevenueReportRequestSchema = z.object({
  propertyId: z.string().uuid(),
  startDate: z.string().date(),
  endDate: z.string().date(),
  groupBy: z.enum(['day', 'week', 'month']).default('day'),
  includeBreakdown: z.boolean().default(false),
});

export const OccupancyDataSchema = z.object({
  date: z.string().date(),
  totalRooms: z.number().int().nonnegative(),
  occupiedRooms: z.number().int().nonnegative(),
  occupancyRate: z.number().min(0).max(1),
  arrivalCount: z.number().int().nonnegative(),
  departureCount: z.number().int().nonnegative(),
});

export const RevenueDataSchema = z.object({
  date: z.string().date(),
  roomRevenue: z.number().nonnegative(),
  totalRevenue: z.number().nonnegative(),
  averageDailyRate: z.number().nonnegative(),
  revenuePerAvailableRoom: z.number().nonnegative(),
  currency: z.string().length(3),
});

// Webhook Schemas
export const WebhookConfigSchema = z.object({
  id: z.string().uuid(),
  propertyId: z.string().uuid(),
  url: z.string().url(),
  events: z.array(z.string()),
  isActive: z.boolean(),
  secret: z.string(),
  headers: z.record(z.string()).optional(),
  retryPolicy: z
    .object({
      maxRetries: z.number().int().nonnegative().default(3),
      retryDelay: z.number().int().positive().default(1000), // milliseconds
    })
    .optional(),
  createdAt: z.string().datetime(),
  updatedAt: z.string().datetime(),
});

export const CreateWebhookRequestSchema = z.object({
  propertyId: z.string().uuid(),
  url: z.string().url(),
  events: z.array(z.string()).min(1),
  headers: z.record(z.string()).optional(),
  retryPolicy: z
    .object({
      maxRetries: z.number().int().nonnegative().default(3),
      retryDelay: z.number().int().positive().default(1000),
    })
    .optional(),
  isActive: z.boolean().default(true),
});

// Error Response Schema
export const ErrorResponseSchema = z.object({
  success: z.literal(false),
  error: z.object({
    code: z.string(),
    message: z.string(),
    details: z.record(z.any()).optional(),
    requestId: z.string().uuid(),
    timestamp: z.string().datetime(),
  }),
});

// Validation Error Schema
export const ValidationErrorSchema = ErrorResponseSchema.extend({
  error: z.object({
    code: z.literal('VALIDATION_ERROR'),
    message: z.string(),
    details: z.object({
      fieldErrors: z.array(
        z.object({
          field: z.string(),
          message: z.string(),
          value: z.any().optional(),
        })
      ),
    }),
    requestId: z.string().uuid(),
    timestamp: z.string().datetime(),
  }),
});

// Health Check Schema
export const HealthCheckResponseSchema = z.object({
  status: z.enum(['healthy', 'degraded', 'unhealthy']),
  timestamp: z.string().datetime(),
  version: z.string(),
  services: z.record(
    z.object({
      status: z.enum(['up', 'down', 'degraded']),
      responseTime: z.number().nonnegative().optional(),
      lastCheck: z.string().datetime(),
      details: z.record(z.any()).optional(),
    })
  ),
});

// Export all types
export type ApiResponse = z.infer<typeof ApiResponseSchema>;
export type PaginatedResponse<T> = z.infer<
  ReturnType<typeof PaginatedResponseSchema<z.ZodType<T>>>
>;
export type CreatePropertyRequest = z.infer<typeof CreatePropertyRequestSchema>;
export type UpdatePropertyRequest = z.infer<typeof UpdatePropertyRequestSchema>;
export type PropertyResponse = z.infer<typeof PropertyResponseSchema>;
export type PropertiesResponse = z.infer<typeof PropertiesResponseSchema>;
export type CreateRoomRequest = z.infer<typeof CreateRoomRequestSchema>;
export type UpdateRoomRequest = z.infer<typeof UpdateRoomRequestSchema>;
export type RoomResponse = z.infer<typeof RoomResponseSchema>;
export type RoomsResponse = z.infer<typeof RoomsResponseSchema>;
export type RoomSearchRequest = z.infer<typeof RoomSearchRequestSchema>;
export type RoomSearchResponse = z.infer<typeof RoomSearchResponseSchema>;
export type CreateGuestRequest = z.infer<typeof CreateGuestRequestSchema>;
export type UpdateGuestRequest = z.infer<typeof UpdateGuestRequestSchema>;
export type GuestResponse = z.infer<typeof GuestResponseSchema>;
export type GuestsResponse = z.infer<typeof GuestsResponseSchema>;
export type CreateReservationRequest = z.infer<typeof CreateReservationRequestSchema>;
export type UpdateReservationRequest = z.infer<typeof UpdateReservationRequestSchema>;
export type ReservationResponse = z.infer<typeof ReservationResponseSchema>;
export type ReservationsResponse = z.infer<typeof ReservationsResponseSchema>;
export type CheckInRequest = z.infer<typeof CheckInRequestSchema>;
export type CheckOutRequest = z.infer<typeof CheckOutRequestSchema>;
export type CreatePaymentRequest = z.infer<typeof CreatePaymentRequestSchema>;
export type PaymentResponse = z.infer<typeof PaymentResponseSchema>;
export type PaymentsResponse = z.infer<typeof PaymentsResponseSchema>;
export type CreateRefundRequest = z.infer<typeof CreateRefundRequestSchema>;
export type AvailabilityRequest = z.infer<typeof AvailabilityRequestSchema>;
export type AvailabilityResponse = z.infer<typeof AvailabilityResponseSchema>;
export type UpdateAvailabilityRequest = z.infer<typeof UpdateAvailabilityRequestSchema>;
export type RatePlan = z.infer<typeof RatePlanSchema>;
export type CreateRatePlanRequest = z.infer<typeof CreateRatePlanRequestSchema>;
export type OccupancyReportRequest = z.infer<typeof OccupancyReportRequestSchema>;
export type RevenueReportRequest = z.infer<typeof RevenueReportRequestSchema>;
export type OccupancyData = z.infer<typeof OccupancyDataSchema>;
export type RevenueData = z.infer<typeof RevenueDataSchema>;
export type WebhookConfig = z.infer<typeof WebhookConfigSchema>;
export type CreateWebhookRequest = z.infer<typeof CreateWebhookRequestSchema>;
export type ErrorResponse = z.infer<typeof ErrorResponseSchema>;
export type ValidationError = z.infer<typeof ValidationErrorSchema>;
export type HealthCheckResponse = z.infer<typeof HealthCheckResponseSchema>;
