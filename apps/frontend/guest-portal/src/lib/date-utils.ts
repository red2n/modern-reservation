/**
 * Date calculation utilities
 * Single Responsibility: Perform date-related calculations
 */

type DateInput = Date | string;

/**
 * Parse date input to Date object
 */
function parseDate(date: DateInput): Date {
  return typeof date === "string" ? new Date(date) : date;
}

/**
 * Calculate number of nights between check-in and check-out dates
 */
export function calculateNights(
  checkIn: DateInput,
  checkOut: DateInput,
): number {
  const checkInDate = parseDate(checkIn);
  const checkOutDate = parseDate(checkOut);

  const diffTime = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  return diffDays;
}

/**
 * Add days to a date
 */
export function addDays(date: DateInput, days: number): Date {
  const result = parseDate(date);
  result.setDate(result.getDate() + days);
  return result;
}

/**
 * Check if date is in the past
 */
export function isPastDate(date: DateInput): boolean {
  return parseDate(date).getTime() < Date.now();
}

/**
 * Check if date is in the future
 */
export function isFutureDate(date: DateInput): boolean {
  return parseDate(date).getTime() > Date.now();
}

/**
 * Check if date is today
 */
export function isToday(date: DateInput): boolean {
  const today = new Date();
  const targetDate = parseDate(date);

  return (
    targetDate.getDate() === today.getDate() &&
    targetDate.getMonth() === today.getMonth() &&
    targetDate.getFullYear() === today.getFullYear()
  );
}

/**
 * Get date range array between two dates
 */
export function getDateRange(startDate: DateInput, endDate: DateInput): Date[] {
  const start = parseDate(startDate);
  const end = parseDate(endDate);
  const dates: Date[] = [];

  const currentDate = new Date(start);

  while (currentDate <= end) {
    dates.push(new Date(currentDate));
    currentDate.setDate(currentDate.getDate() + 1);
  }

  return dates;
}
