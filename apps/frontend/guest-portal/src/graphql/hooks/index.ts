/**
 * GraphQL Hooks Index
 * Single Responsibility: Centralized exports for all GraphQL hooks
 */

// Payment hooks
export {
  useCreatePaymentIntent,
  useProcessPayment,
  useRequestRefund,
} from "./usePayment";
// Property hooks
export {
  useAvailableRooms,
  usePropertyDetails,
  usePropertyReviews,
  useSearchProperties,
} from "./useProperty";
// Reservation hooks
export {
  useCancelReservation,
  useCheckAvailability,
  useCreateReservation,
  useMyReservations,
  useReservationByConfirmation,
  useReservationDetails,
  useUpdateReservation,
} from "./useReservation";
