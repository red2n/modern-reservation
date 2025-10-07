/**
 * Date formatting utilities
 * Single Responsibility: Format dates for display
 */

type DateInput = Date | string;

/**
 * Parse date input to Date object
 */
function parseDate(date: DateInput): Date {
  return typeof date === "string" ? new Date(date) : date;
}

/**
 * Format date in short format (e.g., Jan 15, 2024)
 */
export function formatDateShort(
  date: DateInput,
  locale: string = "en-US",
): string {
  return new Intl.DateTimeFormat(locale, {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(parseDate(date));
}

/**
 * Format date in long format (e.g., Monday, January 15, 2024)
 */
export function formatDateLong(
  date: DateInput,
  locale: string = "en-US",
): string {
  return new Intl.DateTimeFormat(locale, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(parseDate(date));
}

/**
 * Format date with time (e.g., Jan 15, 2024, 3:30 PM)
 */
export function formatDateTime(
  date: DateInput,
  locale: string = "en-US",
): string {
  return new Intl.DateTimeFormat(locale, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
  }).format(parseDate(date));
}

/**
 * Format relative time (e.g., "2 days ago", "in 3 hours")
 */
export function formatRelativeTime(
  date: DateInput,
  locale: string = "en-US",
): string {
  const now = new Date();
  const targetDate = parseDate(date);
  const diffMs = targetDate.getTime() - now.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: "auto" });

  if (Math.abs(diffDays) > 0) {
    return rtf.format(diffDays, "day");
  }
  if (Math.abs(diffHours) > 0) {
    return rtf.format(diffHours, "hour");
  }
  if (Math.abs(diffMinutes) > 0) {
    return rtf.format(diffMinutes, "minute");
  }
  return rtf.format(diffSeconds, "second");
}

/**
 * Format date range (e.g., "Jan 15 - Jan 20, 2024")
 */
export function formatDateRange(
  startDate: DateInput,
  endDate: DateInput,
  locale: string = "en-US",
): string {
  const start = parseDate(startDate);
  const end = parseDate(endDate);

  const sameYear = start.getFullYear() === end.getFullYear();
  const sameMonth = sameYear && start.getMonth() === end.getMonth();

  if (sameMonth) {
    const monthYear = new Intl.DateTimeFormat(locale, {
      month: "short",
      year: "numeric",
    }).format(start);
    return `${start.getDate()} - ${end.getDate()}, ${monthYear}`;
  }

  return `${formatDateShort(start, locale)} - ${formatDateShort(end, locale)}`;
}
