/**
 * Payment GraphQL Hooks
 * Single Responsibility: Custom hooks for payment mutations
 */

"use client";

import { useMutation } from "@apollo/client/react";
import {
  CREATE_PAYMENT_INTENT,
  PROCESS_PAYMENT,
  REQUEST_REFUND,
} from "@modern-reservation/graphql-client";

/**
 * Hook to process a payment
 */
export function useProcessPayment() {
  return useMutation(PROCESS_PAYMENT, {
    refetchQueries: ["GetReservationDetails"],
  });
}

/**
 * Hook to create a payment intent
 */
export function useCreatePaymentIntent() {
  return useMutation(CREATE_PAYMENT_INTENT);
}

/**
 * Hook to request a refund
 */
export function useRequestRefund() {
  return useMutation(REQUEST_REFUND, {
    refetchQueries: ["GetReservationDetails"],
  });
}
