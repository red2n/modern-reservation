import { z } from 'zod';
import {
  UUIDSchema,
  TimestampSchema,
  AuditFieldsSchema,
  MoneyAmountSchema,
  CurrencyCodeSchema,
  AddressSchema,
} from './common';

// =============================================================================
// PAYMENT SCHEMAS
// =============================================================================

// Payment Status - Matches DB enum (UPPERCASE)
export const PaymentStatusSchema = z.enum([
  'PENDING',
  'AUTHORIZED',
  'CAPTURED',
  'COMPLETED',
  'FAILED',
  'CANCELLED',
  'REFUNDED',
  'PARTIALLY_REFUNDED',
  'EXPIRED',
  'PROCESSING',
  'DECLINED'
]);

// Payment Method - Matches DB enum (UPPERCASE)
export const PaymentMethodTypeSchema = z.enum([
  'CREDIT_CARD',
  'DEBIT_CARD',
  'CASH',
  'BANK_TRANSFER',
  'PAYPAL',
  'STRIPE',
  'APPLE_PAY',
  'GOOGLE_PAY',
  'CRYPTOCURRENCY',
  'CHECK',
  'WIRE_TRANSFER',
  'OTHER'
]);

// Transaction Type - Matches DB enum (UPPERCASE)
export const TransactionTypeSchema = z.enum([
  'CHARGE',
  'REFUND',
  'AUTHORIZATION',
  'CAPTURE',
  'VOID',
  'ADJUSTMENT',
  'CHARGEBACK',
  'REVERSAL'
]);

export const PaymentMethodSchema = z.object({
  id: UUIDSchema,
  guestId: UUIDSchema,

  // Payment Method Information
  type: PaymentMethodTypeSchema,
  provider: z.string().max(50).optional(),

  // Tokenized Information (PCI Compliance)
  token: z.string().min(1).max(255),
  lastFour: z.string().length(4).optional(),
  expiryMonth: z.number().int().min(1).max(12).optional(),
  expiryYear: z.number().int().min(2023).optional(),
  cardholderName: z.string().max(255).optional(),

  // Billing Address
  billingAddress: AddressSchema.optional(),

  // Status
  isDefault: z.boolean().default(false),
  isVerified: z.boolean().default(false),
  status: z.enum(['active', 'expired', 'blocked']).default('active'),

  ...AuditFieldsSchema.shape,
});

export const PaymentTransactionSchema = z.object({
  id: UUIDSchema,
  reservationId: UUIDSchema,
  paymentMethodId: UUIDSchema.optional(),

  // Transaction Information
  transactionType: TransactionTypeSchema,
  amount: MoneyAmountSchema,
  currencyCode: CurrencyCodeSchema,

  // Gateway Information
  gatewayProvider: z.string().max(50).optional(),
  gatewayTransactionId: z.string().max(255).optional(),
  gatewayReference: z.string().max(255).optional(),
  gatewayResponse: z.record(z.any()).default({}),

  // Status
  status: PaymentStatusSchema.default('PENDING'),
  processedAt: TimestampSchema.optional(),
  settledAt: TimestampSchema.optional(),

  // Risk and Fraud
  riskScore: z.number().min(0).max(100).optional(),
  riskFlags: z.record(z.any()).default({}),

  // Failure Information
  failureReason: z.string().optional(),
  failureCode: z.string().max(50).optional(),
  retryCount: z.number().int().min(0).default(0),

  ...AuditFieldsSchema.shape,
});

// Payment Gateway Configuration
export const PaymentGatewaySchema = z.object({
  id: UUIDSchema,
  name: z.string().min(1).max(100),
  provider: z.enum(['stripe', 'square', 'paypal', 'adyen', 'braintree', 'authorize_net']),
  isActive: z.boolean().default(true),
  priority: z.number().int().min(1).default(1),
  supportedPaymentMethods: z.array(z.string()),
  supportedCurrencies: z.array(z.string()),
  configuration: z.record(z.any()).default({}),
  feeStructure: z.object({
    percentageFee: z.number().min(0).max(1).default(0),
    fixedFee: MoneyAmountSchema.default(0),
    currencyCode: CurrencyCodeSchema.default('USD'),
  }),
  ...AuditFieldsSchema.shape,
});

// Payment Plan (for installments)
export const PaymentPlanSchema = z.object({
  id: UUIDSchema,
  reservationId: UUIDSchema,
  planType: z.enum(['full_payment', 'deposit', 'installments']),
  totalAmount: MoneyAmountSchema,
  depositAmount: MoneyAmountSchema.optional(),
  remainingAmount: MoneyAmountSchema.optional(),
  installmentCount: z.number().int().min(1).optional(),
  installmentAmount: MoneyAmountSchema.optional(),
  firstPaymentDate: TimestampSchema,
  finalPaymentDate: TimestampSchema,
  autoPayEnabled: z.boolean().default(false),
  status: z.enum(['active', 'completed', 'cancelled', 'defaulted']).default('active'),
  ...AuditFieldsSchema.shape,
});

// Payment Schedule (for payment plans)
export const PaymentScheduleSchema = z.object({
  id: UUIDSchema,
  paymentPlanId: UUIDSchema,
  installmentNumber: z.number().int().min(1),
  dueDate: TimestampSchema,
  amount: MoneyAmountSchema,
  status: z.enum(['pending', 'paid', 'overdue', 'failed', 'cancelled']).default('pending'),
  paidAt: TimestampSchema.optional(),
  paidAmount: MoneyAmountSchema.optional(),
  transactionId: UUIDSchema.optional(),
  remindersSent: z.number().int().min(0).default(0),
  lastReminderAt: TimestampSchema.optional(),
  ...AuditFieldsSchema.shape,
});

// Refund Request
export const RefundRequestSchema = z.object({
  id: UUIDSchema,
  reservationId: UUIDSchema,
  originalTransactionId: UUIDSchema,
  requestedAmount: MoneyAmountSchema,
  approvedAmount: MoneyAmountSchema.optional(),
  reason: z.enum(['cancellation', 'no_show', 'service_failure', 'dispute', 'goodwill', 'other']),
  description: z.string().max(1000).optional(),
  requestedBy: UUIDSchema,
  approvedBy: UUIDSchema.optional(),
  processedBy: UUIDSchema.optional(),
  status: z.enum(['requested', 'approved', 'processed', 'completed', 'rejected', 'failed']).default('requested'),
  requestedAt: TimestampSchema,
  approvedAt: TimestampSchema.optional(),
  processedAt: TimestampSchema.optional(),
  completedAt: TimestampSchema.optional(),
  refundTransactionId: UUIDSchema.optional(),
  notes: z.string().max(1000).optional(),
  ...AuditFieldsSchema.shape,
});

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type PaymentStatus = z.infer<typeof PaymentStatusSchema>;
export type PaymentMethod = z.infer<typeof PaymentMethodSchema>;
export type PaymentTransaction = z.infer<typeof PaymentTransactionSchema>;
export type PaymentGateway = z.infer<typeof PaymentGatewaySchema>;
export type PaymentPlan = z.infer<typeof PaymentPlanSchema>;
export type PaymentSchedule = z.infer<typeof PaymentScheduleSchema>;
export type RefundRequest = z.infer<typeof RefundRequestSchema>;
export type PaymentMethodType = z.infer<typeof PaymentMethodTypeSchema>;
export type TransactionType = z.infer<typeof TransactionTypeSchema>;
