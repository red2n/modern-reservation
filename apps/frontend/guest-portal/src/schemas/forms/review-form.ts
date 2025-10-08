/**
 * Review form validation schema
 */

import { z } from 'zod';

const ratingSchema = z
  .number()
  .int()
  .min(1, 'Rating must be at least 1')
  .max(5, 'Rating must be at most 5');

export const reviewFormSchema = z.object({
  overallRating: ratingSchema,

  // Detailed ratings (optional)
  cleanliness: ratingSchema.optional(),
  communication: ratingSchema.optional(),
  checkIn: ratingSchema.optional(),
  accuracy: ratingSchema.optional(),
  location: ratingSchema.optional(),
  value: ratingSchema.optional(),

  // Review content
  comment: z
    .string()
    .min(10, 'Review must be at least 10 characters')
    .max(1000, 'Review must be at most 1000 characters'),

  // Images (file uploads handled separately)
  images: z.array(z.instanceof(File)).max(5, 'Maximum 5 images').optional(),
});

export type ReviewFormData = z.infer<typeof reviewFormSchema>;
