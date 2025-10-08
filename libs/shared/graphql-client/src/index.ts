/**
 * Shared GraphQL Client - Main Export
 * Single Responsibility: Export all GraphQL operations and utilities
 */

// Apollo Client Configuration
export {
  createApolloClient,
  getClientApolloClient,
  getServerApolloClient,
  resetApolloClient,
} from './apollo-client';

// Apollo Provider
export { ApolloProvider } from './apollo-provider';

// Fragments
export {
  ADDRESS_FRAGMENT,
  GUEST_FRAGMENT,
  IMAGE_FRAGMENT,
  PROPERTY_BASIC_FRAGMENT,
  RESERVATION_FRAGMENT,
  ROOM_FRAGMENT,
  ROOM_TYPE_FRAGMENT,
} from './fragments/common';
// Payment Mutations
export {
  CREATE_PAYMENT_INTENT,
  PROCESS_PAYMENT,
  REQUEST_REFUND,
} from './mutations/payment';
// Reservation Mutations
export {
  CANCEL_RESERVATION,
  CHECK_AVAILABILITY,
  CREATE_RESERVATION,
  UPDATE_RESERVATION,
} from './mutations/reservation';
// Property Queries
export {
  GET_AVAILABLE_ROOMS,
  GET_PROPERTY_DETAILS,
  GET_PROPERTY_REVIEWS,
  SEARCH_PROPERTIES,
} from './queries/property';
// Reservation Queries
export {
  GET_MY_RESERVATIONS,
  GET_RESERVATION_BY_CONFIRMATION,
  GET_RESERVATION_DETAILS,
} from './queries/reservation';
