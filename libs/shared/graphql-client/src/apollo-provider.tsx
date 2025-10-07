/**
 * Apollo Provider for Client Components
 * Single Responsibility: Wrap application with Apollo Provider
 */

"use client";

import { ApolloProvider as ApolloClientProvider } from "@apollo/client/react";
import { getClientApolloClient } from "./apollo-client";

/**
 * Apollo Provider wrapper for client components
 */
export function ApolloProvider({ children }: { children: React.ReactNode }) {
  const client = getClientApolloClient();

  return (
    <ApolloClientProvider client={client}>{children}</ApolloClientProvider>
  );
}
