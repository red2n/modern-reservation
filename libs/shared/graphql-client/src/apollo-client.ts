/**
 * Apollo Client Configuration for Next.js App Router
 * Single Responsibility: Configure and create Apollo Client instance
 */

import { ApolloClient, ApolloLink, from, HttpLink, InMemoryCache } from '@apollo/client';
import { onError } from '@apollo/client/link/error/index.js';
import { RetryLink } from '@apollo/client/link/retry/index.js';

// GraphQL API endpoint
const GRAPHQL_ENDPOINT =
  process.env.NEXT_PUBLIC_GRAPHQL_ENDPOINT || 'http://localhost:8080/graphql';

/**
 * HTTP Link for GraphQL requests
 */
const httpLink = new HttpLink({
  uri: GRAPHQL_ENDPOINT,
  credentials: 'include', // Include cookies for authentication
});

/**
 * Error Link for handling GraphQL and network errors
 */
const errorLink = onError((errorResponse: any) => {
  const { graphQLErrors, networkError } = errorResponse;

  if (graphQLErrors && Array.isArray(graphQLErrors)) {
    graphQLErrors.forEach((error: any) => {
      console.error('[GraphQL error]:', error.message, error.extensions);

      // Handle authentication errors
      if (error.extensions?.code === 'UNAUTHENTICATED') {
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
      }
    });
  }

  if (networkError) {
    console.error('[Network error]:', networkError);
  }
});

/**
 * Retry Link for failed requests
 */
const retryLink = new RetryLink({
  delay: {
    initial: 300,
    max: 3000,
    jitter: true,
  },
  attempts: {
    max: 3,
    retryIf: (error) => {
      // Retry on network errors and 5xx server errors
      return !!error && 'statusCode' in error && (error.statusCode as number) >= 500;
    },
  },
});

/**
 * Auth Link for adding authentication headers
 */
const authLink = new ApolloLink((operation, forward) => {
  // Get token from localStorage or cookies
  const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;

  // Add authorization header if token exists
  operation.setContext(({ headers = {} }) => ({
    headers: {
      ...headers,
      ...(token && { authorization: `Bearer ${token}` }),
    },
  }));

  return forward(operation);
});

/**
 * Apollo Cache Configuration
 */
const cache = new InMemoryCache({
  typePolicies: {
    Query: {
      fields: {
        // Pagination for properties
        searchProperties: {
          keyArgs: ['filters', 'sort'],
          merge(existing, incoming, { args }) {
            if (!existing || args?.pagination?.page === 1) {
              return incoming;
            }
            return {
              ...incoming,
              edges: [...(existing.edges || []), ...(incoming.edges || [])],
            };
          },
        },
        // Pagination for reservations
        myReservations: {
          keyArgs: ['filters'],
          merge(existing, incoming, { args }) {
            if (!existing || args?.pagination?.page === 1) {
              return incoming;
            }
            return {
              ...incoming,
              edges: [...(existing.edges || []), ...(incoming.edges || [])],
            };
          },
        },
      },
    },
    Property: {
      keyFields: ['id'],
    },
    Room: {
      keyFields: ['id'],
    },
    Reservation: {
      keyFields: ['id'],
    },
    Guest: {
      keyFields: ['id'],
    },
  },
});

/**
 * Create Apollo Client instance
 */
export function createApolloClient() {
  return new ApolloClient({
    link: from([authLink, errorLink, retryLink, httpLink]),
    cache,
    defaultOptions: {
      watchQuery: {
        fetchPolicy: 'cache-and-network',
        errorPolicy: 'all',
      },
      query: {
        fetchPolicy: 'network-only',
        errorPolicy: 'all',
      },
      mutate: {
        errorPolicy: 'all',
      },
    },
  });
}

/**
 * Get Apollo Client for Server Components
 */
export function getServerApolloClient(): ApolloClient {
  return createApolloClient();
}

/**
 * Singleton Apollo Client instance for client-side usage
 */
let clientApolloClient: ApolloClient | null = null;

export function getClientApolloClient(): ApolloClient {
  if (!clientApolloClient) {
    clientApolloClient = createApolloClient();
  }
  return clientApolloClient;
}

/**
 * Reset Apollo Client (useful for logout)
 */
export async function resetApolloClient() {
  if (clientApolloClient) {
    await clientApolloClient.clearStore();
  }
}
