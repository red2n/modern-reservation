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
} from "./apollo-client";

// Apollo Provider
export { ApolloProvider } from "./apollo-provider";

// Fragments
export {
  ADDRESS_FRAGMENT,
  IMAGE_FRAGMENT,
  PROPERTY_BASIC_FRAGMENT,
  ROOM_TYPE_FRAGMENT,
  ROOM_FRAGMENT,
  GUEST_FRAGMENT,
  RESERVATION_FRAGMENT,
} from "./fragments/common";

// Property Queries
export {
  SEARCH_PROPERTIES,
  GET_PROPERTY_DETAILS,
  GET_AVAILABLE_ROOMS,
  GET_PROPERTY_REVIEWS,
} from "./queries/property";

// Reservation Queries
export {
  GET_MY_RESERVATIONS,
  GET_RESERVATION_DETAILS,
  GET_RESERVATION_BY_CONFIRMATION,
} from "./queries/reservation";

// Reservation Mutations
export {
  CREATE_RESERVATION,
  UPDATE_RESERVATION,
  CANCEL_RESERVATION,
  CHECK_AVAILABILITY,
} from "./mutations/reservation";

// Payment Mutations
export {
  PROCESS_PAYMENT,
  CREATE_PAYMENT_INTENT,
  REQUEST_REFUND,
} from "./mutations/payment";
