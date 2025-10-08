/**
 * Booking form validation schema
 */

import { z } from 'zod';
import { EmailSchema, PhoneSchema } from '../index';

export const bookingFormSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
  email: EmailSchema,
  phone: PhoneSchema,
  country: z.string().length(2, 'Invalid country code'),
  specialRequests: z.string().max(500, 'Maximum 500 characters').optional(),
  arrivalTime: z
    .string()
    .regex(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, 'Invalid time format (HH:MM)')
    .optional(),
  termsAccepted: z.boolean().refine((val) => val === true, {
    message: 'You must accept the terms and conditions',
  }),
});

export type BookingFormData = z.infer<typeof bookingFormSchema>;
