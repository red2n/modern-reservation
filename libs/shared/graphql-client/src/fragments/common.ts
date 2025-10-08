/**
 * Common GraphQL Fragments
 * Single Responsibility: Define reusable GraphQL fragments
 */

import { gql } from '@apollo/client';

/**
 * Address Fragment
 */
export const ADDRESS_FRAGMENT = gql`
  fragment AddressFields on Address {
    line1
    line2
    city
    state
    country
    postalCode
  }
`;

/**
 * Image Fragment
 */
export const IMAGE_FRAGMENT = gql`
  fragment ImageFields on Image {
    url
    alt
    width
    height
    isPrimary
  }
`;

/**
 * Property Basic Info Fragment
 */
export const PROPERTY_BASIC_FRAGMENT = gql`
  ${ADDRESS_FRAGMENT}

  fragment PropertyBasicFields on Property {
    id
    name
    type
    status
    address {
      ...AddressFields
    }
    phone
    email
  }
`;

/**
 * Room Type Fragment
 */
export const ROOM_TYPE_FRAGMENT = gql`
  fragment RoomTypeFields on RoomType {
    id
    code
    name
    description
    maxOccupancy
    maxAdults
    maxChildren
    baseRate
    minRate
    maxRate
  }
`;

/**
 * Room Fragment
 */
export const ROOM_FRAGMENT = gql`
  ${ROOM_TYPE_FRAGMENT}

  fragment RoomFields on Room {
    id
    roomNumber
    floor
    status
    roomType {
      ...RoomTypeFields
    }
  }
`;

/**
 * Guest Fragment
 */
export const GUEST_FRAGMENT = gql`
  ${ADDRESS_FRAGMENT}

  fragment GuestFields on Guest {
    id
    firstName
    lastName
    email
    phone
    loyaltyTier
    address {
      ...AddressFields
    }
  }
`;

/**
 * Reservation Fragment
 */
export const RESERVATION_FRAGMENT = gql`
  ${PROPERTY_BASIC_FRAGMENT}
  ${ROOM_FRAGMENT}
  ${GUEST_FRAGMENT}

  fragment ReservationFields on Reservation {
    id
    confirmationNumber
    checkInDate
    checkOutDate
    nights
    status
    guestCountAdults
    guestCountChildren
    guestCountInfants
    baseAmount
    taxesAmount
    totalAmount
    currencyCode
    property {
      ...PropertyBasicFields
    }
    assignedRooms {
      ...RoomFields
    }
    primaryGuest {
      ...GuestFields
    }
  }
`;
