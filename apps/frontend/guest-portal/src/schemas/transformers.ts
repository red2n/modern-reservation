/**
 * Transformers for converting between backend (lowercase) and frontend (UPPERCASE) formats
 */

/**
 * Transform backend enum value (lowercase) to frontend display (UPPERCASE)
 */
export function toDisplayCase<T extends string>(value: T): Uppercase<T> {
  return value.toUpperCase() as Uppercase<T>;
}

/**
 * Transform frontend enum value (UPPERCASE) to backend (lowercase)
 */
export function toBackendCase<T extends string>(value: T): Lowercase<T> {
  return value.toLowerCase() as Lowercase<T>;
}

/**
 * Transform object keys from snake_case to camelCase
 */
export function toCamelCase<T extends Record<string, any>>(obj: T): any {
  if (obj === null || typeof obj !== 'object') return obj;

  if (Array.isArray(obj)) {
    return obj.map((item) => toCamelCase(item));
  }

  const result: Record<string, any> = {};

  for (const [key, value] of Object.entries(obj)) {
    const camelKey = key.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
    result[camelKey] = typeof value === 'object' ? toCamelCase(value) : value;
  }

  return result;
}

/**
 * Transform object keys from camelCase to snake_case
 */
export function toSnakeCase<T extends Record<string, any>>(obj: T): any {
  if (obj === null || typeof obj !== 'object') return obj;

  if (Array.isArray(obj)) {
    return obj.map((item) => toSnakeCase(item));
  }

  const result: Record<string, any> = {};

  for (const [key, value] of Object.entries(obj)) {
    const snakeKey = key.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`);
    result[snakeKey] = typeof value === 'object' ? toSnakeCase(value) : value;
  }

  return result;
}
