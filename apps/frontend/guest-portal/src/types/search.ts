/**
 * Search and filter type definitions
 * Single Responsibility: Define search-related types only
 */

import type { GuestCount, Property, PropertyType } from "./index";

// Amenity types (frontend-specific until backend schema is updated)
export type AmenityCategory =
  | "wifi"
  | "parking"
  | "pool"
  | "gym"
  | "spa"
  | "restaurant"
  | "bar"
  | "room_service"
  | "concierge"
  | "pet_friendly";

export type SortOption =
  | "PRICE_LOW_TO_HIGH"
  | "PRICE_HIGH_TO_LOW"
  | "RATING"
  | "POPULARITY"
  | "DISTANCE";

// =============================================================================
// Base search interfaces (Interface Segregation Principle)
// =============================================================================

/**
 * Base search parameters - required fields only
 */
export interface BaseSearchParams {
  destination: string;
  checkInDate: string;
  checkOutDate: string;
  guestCount: GuestCount;
}

/**
 * Filter parameters - optional filtering criteria
 */
export interface SearchFilterParams {
  propertyTypes?: PropertyType[];
  priceRange?: PriceRange;
  amenities?: AmenityCategory[];
  rating?: number;
}

/**
 * Pagination and sorting parameters
 */
export interface SearchPaginationParams {
  sortBy?: SortOption;
  page?: number;
  limit?: number;
}

/**
 * Complete search parameters - composition of all search interfaces
 */
export interface SearchParams
  extends BaseSearchParams,
    SearchFilterParams,
    SearchPaginationParams {}

export interface PriceRange {
  min: number;
  max: number;
  currency: string;
}

export interface SearchResults {
  properties: Property[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
  filters?: SearchFilters;
}

export interface SearchFilters {
  availablePropertyTypes: PropertyType[];
  priceRange: PriceRange;
  availableAmenities: AmenityCategory[];
}

export interface LocationSuggestion {
  id: string;
  name: string;
  type: "CITY" | "COUNTRY" | "PROPERTY";
  description?: string;
}
