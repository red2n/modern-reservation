/**
 * Payment GraphQL Mutations
 * Single Responsibility: Define payment-related mutations
 */

import { gql } from '@apollo/client';

/**
 * Process Payment Mutation
 */
export const PROCESS_PAYMENT = gql`
  mutation ProcessPayment($input: ProcessPaymentInput!) {
    processPayment(input: $input) {
      payment {
        id
        paymentReference
        amount
        currencyCode
        status
        paymentMethod
        transactionType
      }
      success
      message
      errors {
        field
        message
      }
    }
  }
`;

/**
 * Create Payment Intent Mutation
 */
export const CREATE_PAYMENT_INTENT = gql`
  mutation CreatePaymentIntent($input: PaymentIntentInput!) {
    createPaymentIntent(input: $input) {
      clientSecret
      paymentIntentId
      amount
      currency
      errors {
        field
        message
      }
    }
  }
`;

/**
 * Request Refund Mutation
 */
export const REQUEST_REFUND = gql`
  mutation RequestRefund($paymentId: UUID!, $amount: Float!, $reason: String) {
    requestRefund(
      paymentId: $paymentId
      amount: $amount
      reason: $reason
    ) {
      success
      message
      refundId
      refundAmount
      errors {
        field
        message
      }
    }
  }
`;
