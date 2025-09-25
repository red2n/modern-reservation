import { z } from 'zod';

// Base event schema for all Kafka messages
export const BaseEventSchema = z.object({
  eventId: z.string().uuid(),
  eventType: z.string(),
  timestamp: z.string().datetime(),
  version: z.string().default('1.0'),
  source: z.string(), // Service that generated the event
  correlationId: z.string().uuid().optional(),
  tenantId: z.string().uuid(),
});

export type BaseEvent = z.infer<typeof BaseEventSchema>;

// Property Management Events
export const PropertyCreatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('property.created'),
  data: z.object({
    propertyId: z.string().uuid(),
    name: z.string(),
    propertyType: z.enum(['hotel', 'resort', 'apartment', 'villa', 'hostel']),
    address: z.string(),
    city: z.string(),
    country: z.string(),
    timezone: z.string(),
    createdBy: z.string().uuid(),
  }),
});

export const PropertyUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('property.updated'),
  data: z.object({
    propertyId: z.string().uuid(),
    changes: z.record(z.any()),
    updatedBy: z.string().uuid(),
  }),
});

// Room Management Events
export const RoomCreatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('room.created'),
  data: z.object({
    roomId: z.string().uuid(),
    propertyId: z.string().uuid(),
    roomNumber: z.string(),
    roomType: z.string(),
    capacity: z.number().int().positive(),
    basePrice: z.number().positive(),
    amenities: z.array(z.string()).default([]),
    isActive: z.boolean().default(true),
    createdBy: z.string().uuid(),
  }),
});

export const RoomUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('room.updated'),
  data: z.object({
    roomId: z.string().uuid(),
    propertyId: z.string().uuid(),
    changes: z.record(z.any()),
    updatedBy: z.string().uuid(),
  }),
});

export const RoomStatusChangedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('room.status.changed'),
  data: z.object({
    roomId: z.string().uuid(),
    propertyId: z.string().uuid(),
    oldStatus: z.enum(['available', 'occupied', 'maintenance', 'out_of_order']),
    newStatus: z.enum(['available', 'occupied', 'maintenance', 'out_of_order']),
    reason: z.string().optional(),
    changedBy: z.string().uuid(),
  }),
});

// Availability Events
export const AvailabilityUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('availability.updated'),
  data: z.object({
    roomId: z.string().uuid(),
    propertyId: z.string().uuid(),
    date: z.string().date(),
    isAvailable: z.boolean(),
    price: z.number().positive().optional(),
    minStay: z.number().int().positive().optional(),
    maxStay: z.number().int().positive().optional(),
    closedToArrival: z.boolean().default(false),
    closedToDeparture: z.boolean().default(false),
    updatedBy: z.string().uuid(),
  }),
});

export const BulkAvailabilityUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('availability.bulk.updated'),
  data: z.object({
    propertyId: z.string().uuid(),
    roomIds: z.array(z.string().uuid()),
    dateRange: z.object({
      startDate: z.string().date(),
      endDate: z.string().date(),
    }),
    updates: z.object({
      isAvailable: z.boolean().optional(),
      price: z.number().positive().optional(),
      minStay: z.number().int().positive().optional(),
      maxStay: z.number().int().positive().optional(),
      closedToArrival: z.boolean().optional(),
      closedToDeparture: z.boolean().optional(),
    }),
    updatedBy: z.string().uuid(),
  }),
});

// Reservation Events
export const ReservationCreatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('reservation.created'),
  data: z.object({
    reservationId: z.string().uuid(),
    confirmationNumber: z.string(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    guestId: z.string().uuid(),
    checkInDate: z.string().date(),
    checkOutDate: z.string().date(),
    adults: z.number().int().positive(),
    children: z.number().int().nonnegative().default(0),
    totalAmount: z.number().positive(),
    currency: z.string().length(3),
    status: z.enum(['pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled', 'no_show']),
    source: z.enum(['direct', 'booking_dot_com', 'expedia', 'airbnb', 'phone', 'walk_in']),
    createdBy: z.string().uuid(),
  }),
});

export const ReservationUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('reservation.updated'),
  data: z.object({
    reservationId: z.string().uuid(),
    changes: z.record(z.any()),
    updatedBy: z.string().uuid(),
  }),
});

export const ReservationStatusChangedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('reservation.status.changed'),
  data: z.object({
    reservationId: z.string().uuid(),
    confirmationNumber: z.string(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    guestId: z.string().uuid(),
    oldStatus: z.enum(['pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled', 'no_show']),
    newStatus: z.enum(['pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled', 'no_show']),
    reason: z.string().optional(),
    changedBy: z.string().uuid(),
  }),
});

export const ReservationCancelledEventSchema = BaseEventSchema.extend({
  eventType: z.literal('reservation.cancelled'),
  data: z.object({
    reservationId: z.string().uuid(),
    confirmationNumber: z.string(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    guestId: z.string().uuid(),
    cancellationReason: z.string(),
    refundAmount: z.number().nonnegative(),
    cancellationFee: z.number().nonnegative().default(0),
    cancelledBy: z.string().uuid(),
  }),
});

// Check-in/Check-out Events
export const CheckInEventSchema = BaseEventSchema.extend({
  eventType: z.literal('guest.checked.in'),
  data: z.object({
    reservationId: z.string().uuid(),
    confirmationNumber: z.string(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    guestId: z.string().uuid(),
    actualCheckInTime: z.string().datetime(),
    roomKeys: z.array(z.string()).optional(),
    specialRequest: z.string().optional(),
    checkedInBy: z.string().uuid(),
  }),
});

export const CheckOutEventSchema = BaseEventSchema.extend({
  eventType: z.literal('guest.checked.out'),
  data: z.object({
    reservationId: z.string().uuid(),
    confirmationNumber: z.string(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    guestId: z.string().uuid(),
    actualCheckOutTime: z.string().datetime(),
    finalBill: z.number().positive(),
    outstandingCharges: z.number().nonnegative().default(0),
    roomCondition: z.enum(['good', 'needs_cleaning', 'maintenance_required', 'damaged']).default('good'),
    checkedOutBy: z.string().uuid(),
  }),
});

// Payment Events
export const PaymentProcessedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('payment.processed'),
  data: z.object({
    paymentId: z.string().uuid(),
    reservationId: z.string().uuid(),
    guestId: z.string().uuid(),
    amount: z.number().positive(),
    currency: z.string().length(3),
    paymentMethod: z.enum(['credit_card', 'debit_card', 'cash', 'bank_transfer', 'digital_wallet']),
    paymentProvider: z.string(),
    transactionId: z.string(),
    status: z.enum(['pending', 'completed', 'failed', 'refunded', 'cancelled']),
    processedAt: z.string().datetime(),
    processedBy: z.string().uuid(),
  }),
});

export const PaymentFailedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('payment.failed'),
  data: z.object({
    paymentId: z.string().uuid(),
    reservationId: z.string().uuid(),
    guestId: z.string().uuid(),
    amount: z.number().positive(),
    currency: z.string().length(3),
    paymentMethod: z.enum(['credit_card', 'debit_card', 'cash', 'bank_transfer', 'digital_wallet']),
    paymentProvider: z.string(),
    failureReason: z.string(),
    errorCode: z.string().optional(),
    failedAt: z.string().datetime(),
  }),
});

export const RefundProcessedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('refund.processed'),
  data: z.object({
    refundId: z.string().uuid(),
    originalPaymentId: z.string().uuid(),
    reservationId: z.string().uuid(),
    guestId: z.string().uuid(),
    refundAmount: z.number().positive(),
    currency: z.string().length(3),
    reason: z.string(),
    processedAt: z.string().datetime(),
    processedBy: z.string().uuid(),
  }),
});

// Guest Events
export const GuestCreatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('guest.created'),
  data: z.object({
    guestId: z.string().uuid(),
    email: z.string().email(),
    firstName: z.string(),
    lastName: z.string(),
    phone: z.string().optional(),
    dateOfBirth: z.string().date().optional(),
    nationality: z.string().optional(),
    loyaltyNumber: z.string().optional(),
    createdBy: z.string().uuid(),
  }),
});

export const GuestUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('guest.updated'),
  data: z.object({
    guestId: z.string().uuid(),
    changes: z.record(z.any()),
    updatedBy: z.string().uuid(),
  }),
});

// Inventory Events
export const InventoryAdjustedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('inventory.adjusted'),
  data: z.object({
    propertyId: z.string().uuid(),
    roomTypeId: z.string().uuid(),
    date: z.string().date(),
    adjustment: z.number().int(),
    reason: z.string(),
    newAvailableCount: z.number().int().nonnegative(),
    adjustedBy: z.string().uuid(),
  }),
});

// Rate Management Events
export const RateUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('rate.updated'),
  data: z.object({
    rateId: z.string().uuid(),
    propertyId: z.string().uuid(),
    roomTypeId: z.string().uuid(),
    ratePlanId: z.string().uuid(),
    dateRange: z.object({
      startDate: z.string().date(),
      endDate: z.string().date(),
    }),
    newRate: z.number().positive(),
    oldRate: z.number().positive().optional(),
    updatedBy: z.string().uuid(),
  }),
});

// Channel Management Events
export const ChannelInventoryUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('channel.inventory.updated'),
  data: z.object({
    channelId: z.string(),
    propertyId: z.string().uuid(),
    roomTypeId: z.string().uuid(),
    date: z.string().date(),
    availableRooms: z.number().int().nonnegative(),
    updatedBy: z.string().uuid(),
  }),
});

export const ChannelRateUpdatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('channel.rate.updated'),
  data: z.object({
    channelId: z.string(),
    propertyId: z.string().uuid(),
    roomTypeId: z.string().uuid(),
    ratePlanId: z.string().uuid(),
    date: z.string().date(),
    newRate: z.number().positive(),
    currency: z.string().length(3),
    updatedBy: z.string().uuid(),
  }),
});

// Housekeeping Events
export const HousekeepingTaskCreatedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('housekeeping.task.created'),
  data: z.object({
    taskId: z.string().uuid(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    taskType: z.enum(['cleaning', 'maintenance', 'inspection', 'deep_clean']),
    priority: z.enum(['low', 'normal', 'high', 'urgent']),
    description: z.string().optional(),
    estimatedDuration: z.number().int().positive().optional(), // minutes
    assignedTo: z.string().uuid().optional(),
    createdBy: z.string().uuid(),
  }),
});

export const HousekeepingTaskCompletedEventSchema = BaseEventSchema.extend({
  eventType: z.literal('housekeeping.task.completed'),
  data: z.object({
    taskId: z.string().uuid(),
    propertyId: z.string().uuid(),
    roomId: z.string().uuid(),
    completedBy: z.string().uuid(),
    completedAt: z.string().datetime(),
    notes: z.string().optional(),
    qualityScore: z.number().min(1).max(5).optional(),
    nextInspectionRequired: z.boolean().default(false),
  }),
});

// Audit Events
export const AuditEventSchema = BaseEventSchema.extend({
  eventType: z.literal('audit.action'),
  data: z.object({
    userId: z.string().uuid(),
    action: z.string(),
    resource: z.string(),
    resourceId: z.string().uuid(),
    oldValues: z.record(z.any()).optional(),
    newValues: z.record(z.any()).optional(),
    ipAddress: z.string().ip().optional(),
    userAgent: z.string().optional(),
    sessionId: z.string().uuid().optional(),
  }),
});

// Union type for all events
export const EventSchema = z.discriminatedUnion('eventType', [
  PropertyCreatedEventSchema,
  PropertyUpdatedEventSchema,
  RoomCreatedEventSchema,
  RoomUpdatedEventSchema,
  RoomStatusChangedEventSchema,
  AvailabilityUpdatedEventSchema,
  BulkAvailabilityUpdatedEventSchema,
  ReservationCreatedEventSchema,
  ReservationUpdatedEventSchema,
  ReservationStatusChangedEventSchema,
  ReservationCancelledEventSchema,
  CheckInEventSchema,
  CheckOutEventSchema,
  PaymentProcessedEventSchema,
  PaymentFailedEventSchema,
  RefundProcessedEventSchema,
  GuestCreatedEventSchema,
  GuestUpdatedEventSchema,
  InventoryAdjustedEventSchema,
  RateUpdatedEventSchema,
  ChannelInventoryUpdatedEventSchema,
  ChannelRateUpdatedEventSchema,
  HousekeepingTaskCreatedEventSchema,
  HousekeepingTaskCompletedEventSchema,
  AuditEventSchema,
]);

export type Event = z.infer<typeof EventSchema>;

// Event type constants for easy reference
export const EventTypes = {
  // Property Events
  PROPERTY_CREATED: 'property.created' as const,
  PROPERTY_UPDATED: 'property.updated' as const,

  // Room Events
  ROOM_CREATED: 'room.created' as const,
  ROOM_UPDATED: 'room.updated' as const,
  ROOM_STATUS_CHANGED: 'room.status.changed' as const,

  // Availability Events
  AVAILABILITY_UPDATED: 'availability.updated' as const,
  BULK_AVAILABILITY_UPDATED: 'availability.bulk.updated' as const,

  // Reservation Events
  RESERVATION_CREATED: 'reservation.created' as const,
  RESERVATION_UPDATED: 'reservation.updated' as const,
  RESERVATION_STATUS_CHANGED: 'reservation.status.changed' as const,
  RESERVATION_CANCELLED: 'reservation.cancelled' as const,

  // Check-in/out Events
  GUEST_CHECKED_IN: 'guest.checked.in' as const,
  GUEST_CHECKED_OUT: 'guest.checked.out' as const,

  // Payment Events
  PAYMENT_PROCESSED: 'payment.processed' as const,
  PAYMENT_FAILED: 'payment.failed' as const,
  REFUND_PROCESSED: 'refund.processed' as const,

  // Guest Events
  GUEST_CREATED: 'guest.created' as const,
  GUEST_UPDATED: 'guest.updated' as const,

  // Inventory Events
  INVENTORY_ADJUSTED: 'inventory.adjusted' as const,

  // Rate Events
  RATE_UPDATED: 'rate.updated' as const,

  // Channel Events
  CHANNEL_INVENTORY_UPDATED: 'channel.inventory.updated' as const,
  CHANNEL_RATE_UPDATED: 'channel.rate.updated' as const,

  // Housekeeping Events
  HOUSEKEEPING_TASK_CREATED: 'housekeeping.task.created' as const,
  HOUSEKEEPING_TASK_COMPLETED: 'housekeeping.task.completed' as const,

  // Audit Events
  AUDIT_ACTION: 'audit.action' as const,
} as const;

// Helper functions for event creation
export const createEvent = <T extends Event>(
  eventType: T['eventType'],
  data: T['data'],
  metadata: {
    source: string;
    tenantId: string;
    correlationId?: string;
  }
): T => {
  return {
    eventId: crypto.randomUUID(),
    eventType,
    timestamp: new Date().toISOString(),
    version: '1.0',
    source: metadata.source,
    correlationId: metadata.correlationId,
    tenantId: metadata.tenantId,
    data,
  } as T;
};

// Kafka topic naming conventions
export const KafkaTopics = {
  PROPERTY_EVENTS: 'reservation.property.events',
  ROOM_EVENTS: 'reservation.room.events',
  AVAILABILITY_EVENTS: 'reservation.availability.events',
  RESERVATION_EVENTS: 'reservation.reservation.events',
  PAYMENT_EVENTS: 'reservation.payment.events',
  GUEST_EVENTS: 'reservation.guest.events',
  INVENTORY_EVENTS: 'reservation.inventory.events',
  RATE_EVENTS: 'reservation.rate.events',
  CHANNEL_EVENTS: 'reservation.channel.events',
  HOUSEKEEPING_EVENTS: 'reservation.housekeeping.events',
  AUDIT_EVENTS: 'reservation.audit.events',
} as const;
