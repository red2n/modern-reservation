import { z } from 'zod';
import { UUIDSchema, TimestampSchema, EmailSchema } from '../entities/domains/common';

// =============================================================================
// AUTHENTICATION API SCHEMAS
// =============================================================================

/**
 * User Role enum - matches frontend AuthContext.tsx and Java AuthService
 * Based on PRD Section 3 - Target Users & Personas
 */
export const UserRoleSchema = z.enum([
  'GUEST',              // Guest User - Public booking interface
  'FRONT_DESK',         // Front Desk Staff - Check-in/out, guest services
  'RESERVATION_MANAGER', // Reservation Manager - Occupancy, rates, groups
  'HOUSEKEEPING',       // Housekeeping Staff - Room status, tasks
  'HOTEL_ADMIN',        // Hotel Administrator - System config, users
  'FINANCE',            // Finance Team - Billing, payments, reports
  'MAINTENANCE',        // Maintenance Staff - Facility management
  'MANAGER',            // Property Manager - Full operational access
  'SYSTEM_ADMIN',       // System Administrator - Full system access
]);

/**
 * Permission constants - matches frontend AuthContext permission matrix
 */
export const PermissionSchema = z.enum([
  // Reservation permissions
  'RESERVATIONS_VIEW',
  'RESERVATIONS_CREATE',
  'RESERVATIONS_MODIFY',
  'RESERVATIONS_CANCEL',

  // Availability permissions
  'AVAILABILITY_VIEW',
  'AVAILABILITY_MANAGE',
  'AVAILABILITY_BLOCK',

  // Rate permissions
  'RATES_VIEW',
  'RATES_MANAGE',

  // Front desk operations
  'CHECKIN_CHECKOUT',
  'GUEST_SERVICES',
  'WALK_INS',

  // Housekeeping permissions
  'HOUSEKEEPING_VIEW',
  'HOUSEKEEPING_MANAGE',

  // Reporting permissions
  'REPORTS_VIEW',
  'REPORTS_EXPORT',
  'ANALYTICS_VIEW',

  // Admin permissions
  'USERS_MANAGE',
  'SYSTEM_CONFIG',
  'AUDIT_VIEW',
]);

/**
 * User Info schema - matches frontend User interface and Java UserInfo DTO
 */
export const UserInfoSchema = z.object({
  id: UUIDSchema,
  tenantId: UUIDSchema,
  email: EmailSchema,
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  role: UserRoleSchema,
  permissions: z.array(PermissionSchema),
  properties: z.array(UUIDSchema), // Array of property IDs user has access to
  isActive: z.boolean(),
  lastLogin: TimestampSchema.optional(),
});

/**
 * Authentication Request schema
 */
export const AuthRequestSchema = z.object({
  email: EmailSchema,
  password: z.string().min(1, 'Password is required'),
});

/**
 * Authentication Response schema - matches Java AuthResponse
 */
export const AuthResponseSchema = z.object({
  user: UserInfoSchema,
  token: z.string().min(1, 'Token is required'),
});

/**
 * Token validation request schema
 */
export const TokenValidationRequestSchema = z.object({
  token: z.string().min(1, 'Token is required'),
});

/**
 * Token validation response schema
 */
export const TokenValidationResponseSchema = z.object({
  valid: z.boolean(),
  user: UserInfoSchema.optional(),
  expiresAt: TimestampSchema.optional(),
});

/**
 * Password change request schema
 */
export const PasswordChangeRequestSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
      'Password must contain uppercase, lowercase, number, and special character'),
  confirmPassword: z.string().min(1, 'Password confirmation is required'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

/**
 * Logout request schema
 */
export const LogoutRequestSchema = z.object({
  token: z.string().optional(),
  everywhere: z.boolean().default(false), // Logout from all devices
});

/**
 * User session info schema
 */
export const SessionInfoSchema = z.object({
  sessionId: UUIDSchema,
  userId: UUIDSchema,
  deviceInfo: z.object({
    userAgent: z.string(),
    ipAddress: z.string(),
    deviceType: z.enum(['desktop', 'mobile', 'tablet', 'api']),
    browser: z.string().optional(),
    os: z.string().optional(),
  }),
  createdAt: TimestampSchema,
  lastActivity: TimestampSchema,
  expiresAt: TimestampSchema,
});

// =============================================================================
// TYPE EXPORTS
// =============================================================================

export type UserRole = z.infer<typeof UserRoleSchema>;
export type Permission = z.infer<typeof PermissionSchema>;
export type UserInfo = z.infer<typeof UserInfoSchema>;
export type AuthRequest = z.infer<typeof AuthRequestSchema>;
export type AuthResponse = z.infer<typeof AuthResponseSchema>;
export type TokenValidationRequest = z.infer<typeof TokenValidationRequestSchema>;
export type TokenValidationResponse = z.infer<typeof TokenValidationResponseSchema>;
export type PasswordChangeRequest = z.infer<typeof PasswordChangeRequestSchema>;
export type LogoutRequest = z.infer<typeof LogoutRequestSchema>;
export type SessionInfo = z.infer<typeof SessionInfoSchema>;

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Get permissions for a specific role
 */
export const getPermissionsForRole = (role: UserRole): Permission[] => {
  switch (role) {
    case 'MANAGER':
    case 'SYSTEM_ADMIN':
      return [
        'RESERVATIONS_VIEW', 'RESERVATIONS_CREATE', 'RESERVATIONS_MODIFY', 'RESERVATIONS_CANCEL',
        'AVAILABILITY_VIEW', 'AVAILABILITY_MANAGE', 'AVAILABILITY_BLOCK',
        'RATES_VIEW', 'RATES_MANAGE',
        'REPORTS_VIEW', 'REPORTS_EXPORT', 'ANALYTICS_VIEW',
        'USERS_MANAGE', 'SYSTEM_CONFIG', 'AUDIT_VIEW',
        'HOUSEKEEPING_VIEW', 'HOUSEKEEPING_MANAGE',
        'CHECKIN_CHECKOUT', 'GUEST_SERVICES', 'WALK_INS',
      ];
    case 'HOTEL_ADMIN':
      return [
        'USERS_MANAGE', 'SYSTEM_CONFIG', 'AUDIT_VIEW', 'REPORTS_VIEW', 'REPORTS_EXPORT',
        'RESERVATIONS_VIEW', 'RESERVATIONS_CREATE', 'RESERVATIONS_MODIFY', 'RESERVATIONS_CANCEL',
        'AVAILABILITY_VIEW', 'AVAILABILITY_MANAGE', 'RATES_VIEW',
      ];
    case 'RESERVATION_MANAGER':
      return [
        'RESERVATIONS_VIEW', 'RESERVATIONS_CREATE', 'RESERVATIONS_MODIFY', 'RESERVATIONS_CANCEL',
        'AVAILABILITY_VIEW', 'AVAILABILITY_MANAGE', 'AVAILABILITY_BLOCK',
        'RATES_VIEW', 'RATES_MANAGE', 'REPORTS_VIEW', 'ANALYTICS_VIEW',
      ];
    case 'FRONT_DESK':
      return [
        'RESERVATIONS_VIEW', 'RESERVATIONS_CREATE', 'RESERVATIONS_MODIFY',
        'CHECKIN_CHECKOUT', 'GUEST_SERVICES', 'WALK_INS',
        'AVAILABILITY_VIEW', 'HOUSEKEEPING_VIEW',
      ];
    case 'HOUSEKEEPING':
      return ['HOUSEKEEPING_VIEW', 'HOUSEKEEPING_MANAGE', 'RESERVATIONS_VIEW'];
    case 'FINANCE':
      return ['REPORTS_VIEW', 'REPORTS_EXPORT', 'ANALYTICS_VIEW', 'RESERVATIONS_VIEW'];
    case 'MAINTENANCE':
      return ['HOUSEKEEPING_VIEW', 'RESERVATIONS_VIEW'];
    case 'GUEST':
      return ['RESERVATIONS_VIEW'];
    default:
      return [];
  }
};

/**
 * Check if user has specific permission
 */
export const hasPermission = (userPermissions: Permission[], permission: Permission): boolean => {
  return userPermissions.includes(permission);
};

/**
 * Check if user has any of the specified permissions
 */
export const hasAnyPermission = (userPermissions: Permission[], permissions: Permission[]): boolean => {
  return permissions.some(permission => userPermissions.includes(permission));
};

/**
 * Check if user has all specified permissions
 */
export const hasAllPermissions = (userPermissions: Permission[], permissions: Permission[]): boolean => {
  return permissions.every(permission => userPermissions.includes(permission));
};
