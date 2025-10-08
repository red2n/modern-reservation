/**
 * Currency formatting utilities
 * Single Responsibility: Format currency values for display
 */

/**
 * Format amount as currency with locale support
 */
export function formatCurrency(
  amount: number,
  currency: string = 'USD',
  locale: string = 'en-US'
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
  }).format(amount);
}

/**
 * Format amount as compact currency (e.g., $1.2K, $3.4M)
 */
export function formatCompactCurrency(
  amount: number,
  currency: string = 'USD',
  locale: string = 'en-US'
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency,
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(amount);
}

/**
 * Parse currency string to number
 */
export function parseCurrency(value: string): number {
  return Number.parseFloat(value.replace(/[^0-9.-]+/g, ''));
}
