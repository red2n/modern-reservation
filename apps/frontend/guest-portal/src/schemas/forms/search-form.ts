/**
 * Search form validation schema
 */

import { z } from "zod";

export const searchFormSchema = z
  .object({
    destination: z.string().min(1, "Please enter a destination"),
    checkInDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid date format"),
    checkOutDate: z
      .string()
      .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid date format"),
    adults: z
      .number()
      .int()
      .min(1, "At least 1 adult required")
      .max(10, "Maximum 10 adults"),
    children: z.number().int().min(0).max(10, "Maximum 10 children"),
    infants: z.number().int().min(0).max(5, "Maximum 5 infants"),
  })
  .refine((data) => new Date(data.checkOutDate) > new Date(data.checkInDate), {
    message: "Check-out date must be after check-in date",
    path: ["checkOutDate"],
  });

export type SearchFormData = z.infer<typeof searchFormSchema>;
