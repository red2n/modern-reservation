// Main export file for all shared schemas and types
export * from './entities';
export * from './events';
export * from './utils/tenant-helpers';

// Export API module separately to avoid conflicts
export * as API from './api';

// Re-export commonly used Zod utilities
export { z } from 'zod';

// Version information
export const SCHEMA_VERSION = '2.0.0';

// Schema validation helpers
export const validateEntity = <T>(schema: z.ZodSchema<T>, data: unknown): T => {
  return schema.parse(data);
};

export const validateEntitySafe = <T>(
  schema: z.ZodSchema<T>,
  data: unknown
): { success: true; data: T } | { success: false; error: z.ZodError } => {
  const result = schema.safeParse(data);
  if (result.success) {
    return { success: true, data: result.data };
  }
  return { success: false, error: result.error };
};

// Common validation patterns
export const VALIDATION_PATTERNS = {
  UUID: /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i,
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^\+?[1-9]\d{1,14}$/,
  TIME: /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/,
  DATE: /^\d{4}-\d{2}-\d{2}$/,
  DATETIME: /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z?$/,
  CURRENCY: /^[A-Z]{3}$/,
  CONFIRMATION_NUMBER: /^[A-Z0-9]{6,12}$/,
} as const;

// Schema transformation utilities
import { z } from 'zod';

export const transformToUpperCase = z.string().transform(str => str.toUpperCase());
export const transformToLowerCase = z.string().transform(str => str.toLowerCase());
export const transformTrimString = z.string().transform(str => str.trim());

export const createOptionalWithDefault = <T>(schema: z.ZodSchema<T>, defaultValue: T) =>
  schema.optional().transform(val => val ?? defaultValue);

// Environment-specific schema configurations
export const createSchemaConfig = (environment: 'development' | 'staging' | 'production') => ({
  strictValidation: environment === 'production',
  allowUnknownFields: environment === 'development',
  validateAsync: environment !== 'development',
});

// Database column name mappings (for ORM compatibility)
export const DB_COLUMN_MAPPINGS = {
  // Snake case to camel case mappings
  created_at: 'createdAt',
  updated_at: 'updatedAt',
  deleted_at: 'deletedAt',
  is_active: 'isActive',
  is_deleted: 'isDeleted',
  tenant_id: 'tenantId',
  property_id: 'propertyId',
  room_id: 'roomId',
  guest_id: 'guestId',
  reservation_id: 'reservationId',
  payment_id: 'paymentId',
  user_id: 'userId',
  check_in_date: 'checkInDate',
  check_out_date: 'checkOutDate',
  total_amount: 'totalAmount',
  confirmation_number: 'confirmationNumber',
  payment_method: 'paymentMethod',
  room_type: 'roomType',
  property_type: 'propertyType',
  base_price: 'basePrice',
  first_name: 'firstName',
  last_name: 'lastName',
  date_of_birth: 'dateOfBirth',
  loyalty_number: 'loyaltyNumber',
  passport_number: 'passportNumber',
  emergency_contact: 'emergencyContact',
  special_requests: 'specialRequests',
  guest_notes: 'guestNotes',
  additional_guests: 'additionalGuests',
  market_segment: 'marketSegment',
  corporate_code: 'corporateCode',
  group_code: 'groupCode',
  rate_code: 'rateCode',
  payment_status: 'paymentStatus',
  transaction_id: 'transactionId',
  payment_provider: 'paymentProvider',
  refund_amount: 'refundAmount',
  cancellation_fee: 'cancellationFee',
  cancellation_reason: 'cancellationReason',
  check_in_time: 'checkInTime',
  check_out_time: 'checkOutTime',
  postal_code: 'postalCode',
  tax_rate: 'taxRate',
  bed_type: 'bedType',
  bed_count: 'bedCount',
  min_stay: 'minStay',
  max_stay: 'maxStay',
  closed_to_arrival: 'closedToArrival',
  closed_to_departure: 'closedToDeparture',
  available_units: 'availableUnits',
  rate_plan_id: 'ratePlanId',
  rate_plan_code: 'ratePlanCode',
  cancellation_policy: 'cancellationPolicy',
  advance_booking_days: 'advanceBookingDays',
  blackout_dates: 'blackoutDates',
  discount_percent: 'discountPercent',
  room_number: 'roomNumber',
  room_status: 'roomStatus',
  housekeeping_status: 'housekeepingStatus',
  maintenance_notes: 'maintenanceNotes',
  actual_check_in_time: 'actualCheckInTime',
  actual_check_out_time: 'actualCheckOutTime',
  room_keys: 'roomKeys',
  deposit_amount: 'depositAmount',
  identification_verified: 'identificationVerified',
  signature_obtained: 'signatureObtained',
  special_instructions: 'specialInstructions',
  room_condition: 'roomCondition',
  additional_charges: 'additionalCharges',
  guest_satisfaction_score: 'guestSatisfactionScore',
  final_bill: 'finalBill',
  outstanding_charges: 'outstandingCharges',
  quality_score: 'qualityScore',
  next_inspection_required: 'nextInspectionRequired',
  task_type: 'taskType',
  estimated_duration: 'estimatedDuration',
  assigned_to: 'assignedTo',
  completed_by: 'completedBy',
  completed_at: 'completedAt',
  old_values: 'oldValues',
  new_values: 'newValues',
  ip_address: 'ipAddress',
  user_agent: 'userAgent',
  session_id: 'sessionId',
  correlation_id: 'correlationId',
  event_id: 'eventId',
  event_type: 'eventType',
} as const;

// Helper function to convert database row to camelCase object
export const convertDbRowToCamelCase = (row: Record<string, any>): Record<string, any> => {
  const converted: Record<string, any> = {};

  for (const [key, value] of Object.entries(row)) {
    const camelKey = DB_COLUMN_MAPPINGS[key as keyof typeof DB_COLUMN_MAPPINGS] || key;
    converted[camelKey] = value;
  }

  return converted;
};

// Helper function to convert camelCase object to snake_case for database
export const convertCamelCaseToDbRow = (obj: Record<string, any>): Record<string, any> => {
  const converted: Record<string, any> = {};

  // Create reverse mapping
  const reverseMapping = Object.fromEntries(
    Object.entries(DB_COLUMN_MAPPINGS).map(([snake, camel]) => [camel, snake])
  );

  for (const [key, value] of Object.entries(obj)) {
    const snakeKey = reverseMapping[key] || key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
    converted[snakeKey] = value;
  }

  return converted;
};
