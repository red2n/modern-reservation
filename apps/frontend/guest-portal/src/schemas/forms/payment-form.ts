/**
 * Payment form validation schema
 */

import { z } from "zod";

const cardNumberSchema = z
  .string()
  .regex(/^\d{13,19}$/, "Card number must be 13-19 digits")
  .transform((val) => val.replace(/\s/g, ""));

const cvvSchema = z.string().regex(/^\d{3,4}$/, "CVV must be 3-4 digits");

const expiryMonthSchema = z
  .string()
  .regex(/^(0[1-9]|1[0-2])$/, "Invalid month (01-12)");

const expiryYearSchema = z.string().regex(/^\d{2}$/, "Invalid year (YY)");

export const paymentFormSchema = z
  .object({
    paymentMethod: z.enum([
      "CREDIT_CARD",
      "DEBIT_CARD",
      "PAYPAL",
      "BANK_TRANSFER",
      "CASH",
    ]),

    // Card details (required for card payments)
    cardNumber: cardNumberSchema.optional(),
    cardHolderName: z.string().min(1).max(100).optional(),
    expiryMonth: expiryMonthSchema.optional(),
    expiryYear: expiryYearSchema.optional(),
    cvv: cvvSchema.optional(),
    saveCard: z.boolean().default(false),

    // Billing address
    billingAddress: z
      .object({
        street: z.string().min(1, "Street address is required").max(255),
        city: z.string().min(1, "City is required").max(100),
        state: z.string().min(1, "State is required").max(100),
        postalCode: z.string().min(1, "Postal code is required").max(20),
        country: z.string().length(2, "Invalid country code"),
      })
      .optional(),
  })
  .refine(
    (data) => {
      // Require card details if payment method is card
      if (
        data.paymentMethod === "CREDIT_CARD" ||
        data.paymentMethod === "DEBIT_CARD"
      ) {
        return (
          data.cardNumber &&
          data.cardHolderName &&
          data.expiryMonth &&
          data.expiryYear &&
          data.cvv
        );
      }
      return true;
    },
    {
      message: "Card details are required for card payments",
      path: ["cardNumber"],
    },
  );

export type PaymentFormData = z.infer<typeof paymentFormSchema>;
export type BillingAddressFormData = PaymentFormData["billingAddress"];
