/**
 * Utility functions - Re-exports from specialized modules
 * Following Single Responsibility Principle (SRP)
 *
 * For new code, prefer importing from specific modules:
 * - import { cn } from '@/lib/css'
 * - import { formatCurrency } from '@/lib/formatters'
 * - import { calculateNights } from '@/lib/date-utils'
 * - import { slugify } from '@/lib/string-utils'
 * - import { debounce } from '@/lib/function-utils'
 */

// CSS utilities
export { cn } from "./css";
// Date utilities
export {
  addDays,
  calculateNights,
  getDateRange,
  isFutureDate,
  isPastDate,
  isToday,
} from "./date-utils";
// Formatters
export {
  formatCompactCurrency,
  formatCurrency,
  parseCurrency,
} from "./formatters/currency";
// Backward compatibility - map old function names to new ones
export {
  formatDateLong,
  formatDateRange,
  formatDateShort,
  formatDateShort as formatDate,
  formatDateTime,
  formatRelativeTime,
} from "./formatters/date";

// Function utilities
export {
  debounce,
  memoize,
  throttle,
} from "./function-utils";
// String utilities
export {
  capitalize,
  getInitials,
  normalizeWhitespace,
  slugify,
  toTitleCase,
  truncate,
} from "./string-utils";
