/**
 * Reservation GraphQL Hooks
 * Single Responsibility: Custom hooks for reservation queries and mutations
 */

'use client';

import { useMutation, useQuery } from '@apollo/client/react';
import {
  CANCEL_RESERVATION,
  CHECK_AVAILABILITY,
  CREATE_RESERVATION,
  GET_MY_RESERVATIONS,
  GET_RESERVATION_BY_CONFIRMATION,
  GET_RESERVATION_DETAILS,
  UPDATE_RESERVATION,
} from '@modern-reservation/graphql-client';

/**
 * Hook to get user's reservations
 */
export function useMyReservations(variables?: {
  status?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  limit?: number;
}) {
  return useQuery(GET_MY_RESERVATIONS, {
    variables,
  });
}

/**
 * Hook to get reservation details
 */
export function useReservationDetails(reservationId: string) {
  return useQuery(GET_RESERVATION_DETAILS, {
    variables: { id: reservationId },
  });
}

/**
 * Hook to get reservation by confirmation number
 */
export function useReservationByConfirmation(confirmationNumber: string) {
  return useQuery(GET_RESERVATION_BY_CONFIRMATION, {
    variables: { confirmationNumber },
  });
}

/**
 * Hook to create a reservation
 */
export function useCreateReservation() {
  return useMutation(CREATE_RESERVATION, {
    refetchQueries: ['GetMyReservations'],
    awaitRefetchQueries: true,
  });
}

/**
 * Hook to update a reservation
 */
export function useUpdateReservation() {
  return useMutation(UPDATE_RESERVATION, {
    refetchQueries: ['GetReservationDetails'],
  });
}

/**
 * Hook to cancel a reservation
 */
export function useCancelReservation() {
  return useMutation(CANCEL_RESERVATION, {
    refetchQueries: ['GetMyReservations', 'GetReservationDetails'],
  });
}

/**
 * Hook to check availability
 */
export function useCheckAvailability() {
  return useMutation(CHECK_AVAILABILITY);
}
