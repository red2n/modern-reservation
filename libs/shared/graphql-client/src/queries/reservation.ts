/**
 * Reservation GraphQL Queries
 * Single Responsibility: Define reservation-related queries
 */

import { gql } from "@apollo/client";
import { RESERVATION_FRAGMENT } from "../fragments/common";

/**
 * Get My Reservations Query
 */
export const GET_MY_RESERVATIONS = gql`
  ${RESERVATION_FRAGMENT}

  query GetMyReservations(
    $status: [ReservationStatus!]
    $page: Int
    $limit: Int
  ) {
    myReservations(
      filters: {
        status: $status
      }
      pagination: {
        page: $page
        limit: $limit
      }
    ) {
      edges {
        node {
          ...ReservationFields
          specialRequests
          createdAt
        }
      }
      pageInfo {
        hasNextPage
        totalCount
      }
    }
  }
`;

/**
 * Get Reservation Details Query
 */
export const GET_RESERVATION_DETAILS = gql`
  ${RESERVATION_FRAGMENT}

  query GetReservationDetails($id: UUID!) {
    reservation(id: $id) {
      ...ReservationFields
      specialRequests
      internalNotes
      estimatedArrivalTime
      feesAmount
      discountAmount
      createdAt
      updatedAt
    }
  }
`;

/**
 * Get Reservation by Confirmation Number Query
 */
export const GET_RESERVATION_BY_CONFIRMATION = gql`
  ${RESERVATION_FRAGMENT}

  query GetReservationByConfirmation($confirmationNumber: String!) {
    reservationByConfirmation(confirmationNumber: $confirmationNumber) {
      ...ReservationFields
      specialRequests
      createdAt
    }
  }
`;
