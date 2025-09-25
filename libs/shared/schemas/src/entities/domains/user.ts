import { z } from 'zod';
import {
  UUIDSchema,
  TimestampSchema,
  AuditFieldsSchema,
  EmailSchema,
  PhoneSchema,
  AddressSchema,
} from './common';

// =============================================================================
// USER MANAGEMENT SCHEMAS
// =============================================================================

// User Schema
export const UserSchema = z.object({
  id: UUIDSchema,
  email: EmailSchema,
  username: z.string().min(3).max(50).optional(),

  // Personal Information
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  phone: PhoneSchema.optional(),
  avatar: z.string().url().optional(),

  // Authentication
  passwordHash: z.string().min(1),
  passwordSalt: z.string().min(1),
  lastPasswordChange: TimestampSchema.optional(),

  // Status
  status: z.enum(['active', 'inactive', 'suspended', 'pending_verification']).default('pending_verification'),
  emailVerified: z.boolean().default(false),
  emailVerifiedAt: TimestampSchema.optional(),
  phoneVerified: z.boolean().default(false),
  phoneVerifiedAt: TimestampSchema.optional(),

  // Authentication & Security
  twoFactorEnabled: z.boolean().default(false),
  twoFactorSecret: z.string().optional(),
  lastLoginAt: TimestampSchema.optional(),
  lastLoginIp: z.string().optional(),
  failedLoginAttempts: z.number().int().min(0).default(0),
  lockedUntil: TimestampSchema.optional(),

  // Profile
  address: AddressSchema.optional(),
  timezone: z.string().default('UTC'),
  language: z.string().default('en'),

  ...AuditFieldsSchema.shape,
});

// Role Schema
export const RoleSchema = z.object({
  id: UUIDSchema,
  name: z.string().min(1).max(100),
  description: z.string().max(500).optional(),
  permissions: z.array(z.string()),
  isSystemRole: z.boolean().default(false),
  isActive: z.boolean().default(true),
  ...AuditFieldsSchema.shape,
});

// User Role Assignment Schema
export const UserRoleSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,
  roleId: UUIDSchema,
  propertyId: UUIDSchema.optional(), // Property-specific role assignment
  assignedBy: UUIDSchema,
  assignedAt: TimestampSchema,
  expiresAt: TimestampSchema.optional(),
  isActive: z.boolean().default(true),
  ...AuditFieldsSchema.shape,
});

// Permission Schema
export const PermissionSchema = z.object({
  id: UUIDSchema,
  name: z.string().min(1).max(100),
  resource: z.string().min(1).max(100), // e.g., 'reservations', 'properties'
  action: z.string().min(1).max(50), // e.g., 'read', 'write', 'delete'
  conditions: z.record(z.any()).optional(), // JSON conditions for fine-grained permissions
  description: z.string().max(500).optional(),
  ...AuditFieldsSchema.shape,
});

// User Session Schema
export const UserSessionSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,
  sessionToken: z.string().min(1),
  refreshToken: z.string().min(1).optional(),
  deviceInfo: z.object({
    userAgent: z.string(),
    ipAddress: z.string(),
    deviceType: z.enum(['desktop', 'mobile', 'tablet', 'api']),
    browser: z.string().optional(),
    os: z.string().optional(),
  }),
  isActive: z.boolean().default(true),
  expiresAt: TimestampSchema,
  lastActivity: TimestampSchema,
  ...AuditFieldsSchema.shape,
});

// Password Reset Token Schema
export const PasswordResetTokenSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,
  token: z.string().min(1),
  expiresAt: TimestampSchema,
  used: z.boolean().default(false),
  usedAt: TimestampSchema.optional(),
  ipAddress: z.string(),
  userAgent: z.string(),
  ...AuditFieldsSchema.shape,
});

// Email Verification Token Schema
export const EmailVerificationTokenSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,
  email: EmailSchema,
  token: z.string().min(1),
  expiresAt: TimestampSchema,
  verified: z.boolean().default(false),
  verifiedAt: TimestampSchema.optional(),
  ...AuditFieldsSchema.shape,
});

// User Audit Log Schema
export const UserAuditLogSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,
  action: z.string().min(1).max(100), // e.g., 'login', 'password_change', 'profile_update'
  resource: z.string().max(100).optional(), // What was accessed/modified
  resourceId: UUIDSchema.optional(),
  oldValues: z.record(z.any()).optional(),
  newValues: z.record(z.any()).optional(),
  ipAddress: z.string(),
  userAgent: z.string(),
  success: z.boolean().default(true),
  errorMessage: z.string().optional(),
  timestamp: TimestampSchema,
  ...AuditFieldsSchema.shape,
});

// User Preferences Schema
export const UserPreferencesSchema = z.object({
  id: UUIDSchema,
  userId: UUIDSchema,

  // Interface Preferences
  theme: z.enum(['light', 'dark', 'auto']).default('auto'),
  language: z.string().default('en'),
  timezone: z.string().default('UTC'),
  dateFormat: z.enum(['MM/DD/YYYY', 'DD/MM/YYYY', 'YYYY-MM-DD']).default('MM/DD/YYYY'),
  timeFormat: z.enum(['12h', '24h']).default('12h'),

  // Notification Preferences
  emailNotifications: z.boolean().default(true),
  smsNotifications: z.boolean().default(false),
  pushNotifications: z.boolean().default(true),

  // Dashboard Settings
  dashboardLayout: z.record(z.any()).default({}),
  defaultProperty: UUIDSchema.optional(),

  ...AuditFieldsSchema.shape,
});

// User Status Enumeration
export const UserStatusSchema = z.enum([
  'active',
  'inactive',
  'suspended',
  'pending_verification'
]);

// Device Type Enumeration
export const DeviceTypeSchema = z.enum(['desktop', 'mobile', 'tablet', 'api']);

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type User = z.infer<typeof UserSchema>;
export type Role = z.infer<typeof RoleSchema>;
export type UserRole = z.infer<typeof UserRoleSchema>;
export type Permission = z.infer<typeof PermissionSchema>;
export type UserSession = z.infer<typeof UserSessionSchema>;
export type PasswordResetToken = z.infer<typeof PasswordResetTokenSchema>;
export type EmailVerificationToken = z.infer<typeof EmailVerificationTokenSchema>;
export type UserAuditLog = z.infer<typeof UserAuditLogSchema>;
export type UserPreferences = z.infer<typeof UserPreferencesSchema>;
export type UserStatus = z.infer<typeof UserStatusSchema>;
export type DeviceType = z.infer<typeof DeviceTypeSchema>;
