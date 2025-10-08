/**
 * Central export file for all type definitions
 * Types are inferred from Zod schemas for single source of truth
 */

import type { z } from 'zod';
import type {
  AddressSchema,
  GuestSchema,
  LoyaltyTierSchema,
  PaymentSchemas,
  PropertySchema,
  PropertyTypeSchema,
  ReservationSchema,
  ReservationStatusSchema,
  RoomSchema,
  RoomStatusSchema,
  RoomTypeSchema,
} from '../schemas';

// =============================================================================
// CORE ENTITY TYPES (from shared schemas)
// =============================================================================

// Common types
export type Address = z.infer<typeof AddressSchema>;

// Property types
export type Property = z.infer<typeof PropertySchema>;
export type PropertyType = z.infer<typeof PropertyTypeSchema>;

// Room types
export type Room = z.infer<typeof RoomSchema>;
export type RoomType = z.infer<typeof RoomTypeSchema>;
export type RoomStatus = z.infer<typeof RoomStatusSchema>;

// Guest types
export type Guest = z.infer<typeof GuestSchema>;
export type LoyaltyTier = z.infer<typeof LoyaltyTierSchema>;

// Reservation types
export type Reservation = z.infer<typeof ReservationSchema>;
export type ReservationStatus = z.infer<typeof ReservationStatusSchema>;

// Payment types
export type PaymentMethod = z.infer<typeof PaymentSchemas.PaymentMethod>;
export type PaymentTransaction = z.infer<typeof PaymentSchemas.PaymentTransaction>;
export type PaymentStatus = z.infer<typeof PaymentSchemas.PaymentStatus>;

// =============================================================================
// FRONTEND-SPECIFIC TYPES
// =============================================================================

// Guest count (UI-specific)
export interface GuestCount {
  adults: number;
  children: number;
  infants: number;
}

// API wrapper types
export type {
  ApiError,
  ApiResponse,
  ApiStatus,
  PaginatedResponse,
  ValidationError,
} from './api';

// Review types (UI-specific)
export type {
  DetailedRatings,
  RatingDistribution,
  Review,
  ReviewResponse,
  ReviewSummary,
} from './review';
// Search types (UI-specific)
export type {
  LocationSuggestion,
  PriceRange,
  SearchFilters,
  SearchParams,
  SearchResults,
  SortOption,
} from './search';

// UI state types
export type {
  DatePickerState,
  DrawerState,
  DropdownState,
  LoadingState,
  ModalState,
  ModalType,
  Tab,
  TabState,
  ToastState,
  ToastType,
} from './ui';
