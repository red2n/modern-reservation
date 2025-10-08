/**
 * Property GraphQL Hooks
 * Single Responsibility: Custom hooks for property queries
 */

'use client';

import { useQuery, useSuspenseQuery } from '@apollo/client/react';
import {
  GET_AVAILABLE_ROOMS,
  GET_PROPERTY_DETAILS,
  GET_PROPERTY_REVIEWS,
  SEARCH_PROPERTIES,
} from '@modern-reservation/graphql-client';

/**
 * Hook to search properties
 */
export function useSearchProperties(
  variables: {
    destination?: string;
    checkInDate: string;
    checkOutDate: string;
    adults: number;
    children?: number;
    propertyTypes?: string[];
    priceRange?: { min: number; max: number; currency: string };
    amenities?: string[];
    rating?: number;
    sortBy?: string;
    page?: number;
    limit?: number;
  },
  options?: Omit<Parameters<typeof useQuery>[1], 'variables'>
) {
  return useQuery(SEARCH_PROPERTIES, {
    variables,
    ...options,
  });
}

/**
 * Hook to get property details (with Suspense)
 */
export function usePropertyDetails(propertyId: string) {
  return useSuspenseQuery(GET_PROPERTY_DETAILS, {
    variables: { id: propertyId },
  });
}

/**
 * Hook to get available rooms
 */
export function useAvailableRooms(variables: {
  propertyId: string;
  checkInDate: string;
  checkOutDate: string;
  adults: number;
  children?: number;
}) {
  return useQuery(GET_AVAILABLE_ROOMS, {
    variables,
  });
}

/**
 * Hook to get property reviews
 */
export function usePropertyReviews(
  propertyId: string,
  options?: {
    page?: number;
    limit?: number;
    sortBy?: string;
  }
) {
  return useQuery(GET_PROPERTY_REVIEWS, {
    variables: {
      propertyId,
      ...options,
    },
  });
}
