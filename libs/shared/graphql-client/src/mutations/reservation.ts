/**
 * Reservation GraphQL Mutations
 * Single Responsibility: Define reservation-related mutations
 */

import { gql } from '@apollo/client';
import { RESERVATION_FRAGMENT } from '../fragments/common';

/**
 * Create Reservation Mutation
 */
export const CREATE_RESERVATION = gql`
  ${RESERVATION_FRAGMENT}

  mutation CreateReservation($input: CreateReservationInput!) {
    createReservation(input: $input) {
      reservation {
        ...ReservationFields
      }
      errors {
        field
        message
      }
    }
  }
`;

/**
 * Update Reservation Mutation
 */
export const UPDATE_RESERVATION = gql`
  ${RESERVATION_FRAGMENT}

  mutation UpdateReservation($id: UUID!, $input: UpdateReservationInput!) {
    updateReservation(id: $id, input: $input) {
      reservation {
        ...ReservationFields
      }
      errors {
        field
        message
      }
    }
  }
`;

/**
 * Cancel Reservation Mutation
 */
export const CANCEL_RESERVATION = gql`
  mutation CancelReservation($id: UUID!, $reason: String) {
    cancelReservation(id: $id, reason: $reason) {
      success
      message
      refundAmount
      errors {
        field
        message
      }
    }
  }
`;

/**
 * Check Availability Mutation
 */
export const CHECK_AVAILABILITY = gql`
  mutation CheckAvailability($input: AvailabilityCheckInput!) {
    checkAvailability(input: $input) {
      available
      rooms {
        roomTypeId
        availableCount
        pricing {
          baseRate
          totalRate
          taxes
          fees
          grandTotal
          currencyCode
        }
      }
      message
    }
  }
`;
