/**
 * Property GraphQL Queries
 * Single Responsibility: Define property-related queries
 */

import { gql } from "@apollo/client";
import {
  IMAGE_FRAGMENT,
  PROPERTY_BASIC_FRAGMENT,
  ROOM_TYPE_FRAGMENT,
} from "../fragments/common";

/**
 * Search Properties Query
 */
export const SEARCH_PROPERTIES = gql`
  ${PROPERTY_BASIC_FRAGMENT}
  ${IMAGE_FRAGMENT}

  query SearchProperties(
    $destination: String
    $checkInDate: Date!
    $checkOutDate: Date!
    $adults: Int!
    $children: Int
    $propertyTypes: [PropertyType!]
    $priceRange: PriceRangeInput
    $amenities: [String!]
    $rating: Float
    $sortBy: SortOption
    $page: Int
    $limit: Int
  ) {
    searchProperties(
      filters: {
        destination: $destination
        checkInDate: $checkInDate
        checkOutDate: $checkOutDate
        guests: {
          adults: $adults
          children: $children
        }
        propertyTypes: $propertyTypes
        priceRange: $priceRange
        amenities: $amenities
        minRating: $rating
      }
      sort: $sortBy
      pagination: {
        page: $page
        limit: $limit
      }
    ) {
      edges {
        node {
          ...PropertyBasicFields
          images {
            ...ImageFields
          }
          averageRating
          totalReviews
          lowestRate
        }
        cursor
      }
      pageInfo {
        hasNextPage
        hasPreviousPage
        startCursor
        endCursor
      }
      totalCount
    }
  }
`;

/**
 * Get Property Details Query
 */
export const GET_PROPERTY_DETAILS = gql`
  ${PROPERTY_BASIC_FRAGMENT}
  ${IMAGE_FRAGMENT}
  ${ROOM_TYPE_FRAGMENT}

  query GetPropertyDetails($id: UUID!) {
    property(id: $id) {
      ...PropertyBasicFields
      description
      checkInTime
      checkOutTime
      images {
        ...ImageFields
      }
      amenities {
        id
        name
        category
        icon
      }
      policies {
        checkInAge
        petPolicy
        smokingPolicy
        cancellationPolicy
      }
      roomTypes {
        ...RoomTypeFields
        amenities {
          id
          name
          icon
        }
        images {
          ...ImageFields
        }
      }
      averageRating
      totalReviews
      detailedRatings {
        cleanliness
        communication
        checkIn
        accuracy
        location
        value
      }
    }
  }
`;

/**
 * Get Available Room Types Query
 */
export const GET_AVAILABLE_ROOMS = gql`
  ${ROOM_TYPE_FRAGMENT}

  query GetAvailableRooms(
    $propertyId: UUID!
    $checkInDate: Date!
    $checkOutDate: Date!
    $adults: Int!
    $children: Int
  ) {
    availableRooms(
      propertyId: $propertyId
      checkInDate: $checkInDate
      checkOutDate: $checkOutDate
      guests: {
        adults: $adults
        children: $children
      }
    ) {
      ...RoomTypeFields
      availableCount
      pricing {
        baseRate
        totalRate
        taxes
        fees
        discounts
        grandTotal
        currencyCode
      }
    }
  }
`;

/**
 * Get Property Reviews Query
 */
export const GET_PROPERTY_REVIEWS = gql`
  query GetPropertyReviews(
    $propertyId: UUID!
    $page: Int
    $limit: Int
    $sortBy: ReviewSortOption
  ) {
    propertyReviews(
      propertyId: $propertyId
      pagination: {
        page: $page
        limit: $limit
      }
      sort: $sortBy
    ) {
      edges {
        node {
          id
          overallRating
          comment
          images
          verified
          createdAt
          guest {
            firstName
            lastName
          }
          detailedRatings {
            cleanliness
            communication
            checkIn
            accuracy
            location
            value
          }
          response {
            message
            createdAt
          }
        }
      }
      pageInfo {
        hasNextPage
        totalCount
      }
    }
  }
`;
