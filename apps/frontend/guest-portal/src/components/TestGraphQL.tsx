/**
 * Test GraphQL Component
 * Single Responsibility: Quick test to verify Apollo Client integration
 */

"use client";

import { useSearchProperties } from "@/graphql/hooks";

export function TestGraphQL() {
  const { data, loading, error } = useSearchProperties({
    checkInDate: "2025-01-15",
    checkOutDate: "2025-01-20",
    adults: 2,
  });

  if (loading) return <div className="p-4">Loading properties...</div>;
  if (error)
    return <div className="p-4 text-red-500">Error: {error.message}</div>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Test GraphQL Integration</h2>
      <pre className="bg-gray-100 p-4 rounded overflow-auto">
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}
