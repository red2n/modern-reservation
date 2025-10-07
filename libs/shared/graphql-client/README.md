# Shared GraphQL Client

Shared GraphQL client configuration, queries, mutations, and fragments for Modern Reservation System.

## Usage

```typescript
// In any frontend portal
import {
  SEARCH_PROPERTIES,
  GET_PROPERTY_DETAILS,
  createApolloClient,
  ApolloProvider
} from '@modern-reservation/graphql-client'
```

## Structure

- `fragments/` - Reusable GraphQL fragments
- `queries/` - GraphQL queries for all portals
- `mutations/` - GraphQL mutations for all operations
- `apollo-client.ts` - Apollo Client configuration
- `apollo-provider.tsx` - React Apollo Provider component

## Features

- ✅ Type-safe GraphQL operations
- ✅ Shared fragments for DRY code
- ✅ Configurable Apollo Client
- ✅ Error handling and retry logic
- ✅ Authentication support
- ✅ Cache management
