/**
 * Review and rating type definitions
 * Single Responsibility: Define review-related types only
 */

// Guest info for reviews (simplified, no need to import full Guest type)
export interface ReviewGuest {
  id: string;
  firstName: string;
  lastName: string;
}

export interface Review {
  id: string;
  propertyId: string;
  guest: ReviewGuest;
  overallRating: number;
  detailedRatings?: DetailedRatings;
  comment: string;
  images?: string[];
  createdAt: string;
  verified: boolean;
  helpful: number;
  response?: ReviewResponse;
}

export interface DetailedRatings {
  cleanliness: number;
  communication: number;
  checkIn: number;
  accuracy: number;
  location: number;
  value: number;
}

export interface ReviewResponse {
  message: string;
  createdAt: string;
  respondedBy: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: RatingDistribution;
  detailedAverages?: DetailedRatings;
}

export interface RatingDistribution {
  5: number;
  4: number;
  3: number;
  2: number;
  1: number;
}
