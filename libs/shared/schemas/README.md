# Zod Schemas

This library contains modular Zod validation schemas for the Modern Reservation System.

## 🏗️ Architecture

The schemas are organized as **modular, domain-focused files** to improve development workflow:

- **Individual Domain Files**: Each business domain has its own focused Zod schema file
- **Master Index**: Centralized exports and collections via `entities/index.ts`
- **Type Safety**: Full TypeScript type inference and validation

## 📁 File Structure

```
schemas/src/
├── entities/
│   ├── domains/                    # Modular schema files
│   │   ├── common.ts              # Shared schemas (UUID, Date, Address, etc.)
│   │   ├── property.ts            # Property & room management schemas
│   │   ├── guest.ts               # Guest management schemas
│   │   ├── reservation.ts         # Reservation & booking schemas
│   │   ├── payment.ts             # Payment & transaction schemas
│   │   ├── availability-rates.ts  # Rate & availability schemas
│   │   └── user.ts                # User management & auth schemas
│   └── index.ts                   # Master index with organized collections
├── api/                           # API request/response schemas
├── events/                        # Event schemas for messaging
└── index.ts                       # Library entry point
```

## 🚀 Usage

### Import Specific Domain Schemas

```typescript
// Import from specific domains
import { PropertySchema, RoomSchema } from '@/schemas/entities/domains/property';
import { GuestSchema } from '@/schemas/entities/domains/guest';
import { ReservationSchema } from '@/schemas/entities/domains/reservation';

// Validate data
const validatedProperty = PropertySchema.parse(propertyData);
const validatedGuest = GuestSchema.parse(guestData);
```

### Import Schema Collections

```typescript
// Import organized collections
import { PropertySchemas, GuestSchemas, ReservationSchemas } from '@/schemas/entities';

// Use specific schema from collection
const property = PropertySchemas.Property.parse(data);
const room = PropertySchemas.Room.parse(roomData);
```

### Backward Compatibility

```typescript
// Legacy import pattern still works
import { EntitySchemas } from '@/schemas/entities';

const reservation = EntitySchemas.Reservation.parse(data);
const guest = EntitySchemas.Guest.parse(guestData);
```

### Type Inference

```typescript
import type { Property, Guest, Reservation } from '@/schemas/entities';

// Types are automatically inferred from schemas
const createReservation = (reservation: Reservation): Promise<Reservation> => {
  // reservation is fully typed
  return api.post('/reservations', reservation);
};
```

## 🔧 Development Benefits

### Why Modular?
- **Focused Files**: Each domain is isolated and easy to understand
- **Reduced Confusion**: No more searching through massive monolithic files
- **Faster Development**: Quick location of relevant schemas and validations
- **Better Collaboration**: Multiple developers can work on different domains
- **Easier Debugging**: Validation errors are contained within specific domain contexts

### Schema Organization Principles
- **Domain-Driven**: Files organized by business domain boundaries
- **Dependency Management**: Common schemas imported by domain-specific files
- **Type Safety**: Full TypeScript integration with automatic type inference
- **Validation**: Comprehensive validation rules with clear error messages

## 📊 Domain Overview

| Domain | File | Schemas | Key Features |
|---------|------|---------|--------------|
| **Common** | `common.ts` | 10+ | UUID, dates, addresses, audit fields |
| **Property** | `property.ts` | 6 | Properties, rooms, types, configurations |
| **Guest** | `guest.ts` | 6 | Guest profiles, preferences, communication |
| **Reservation** | `reservation.ts` | 8 | Bookings, statuses, modifications, services |
| **Payment** | `payment.ts` | 9 | Transactions, methods, gateways, refunds |
| **Availability** | `availability-rates.ts` | 8 | Rate plans, pricing, restrictions, calendar |
| **User** | `user.ts` | 10 | Authentication, roles, permissions, sessions |

**Total:** 57+ schemas across 7 focused domains

## 🛠️ Common Patterns

### Schema Composition

```typescript
// Common fields are reused across domains
export const PropertySchema = z.object({
  id: UUIDSchema,
  name: z.string().min(1).max(255),
  // ... domain-specific fields
  ...AuditFieldsSchema.shape,  // Reused audit fields
  ...SoftDeleteFieldsSchema.shape,  // Reused soft delete
});
```

### Enum Definitions

```typescript
// Clear, type-safe enumerations
export const ReservationStatusSchema = z.enum([
  'inquiry',
  'confirmed',
  'checked_in',
  'checked_out',
  'cancelled'
]);

export type ReservationStatus = z.infer<typeof ReservationStatusSchema>;
```

### Nested Validation

```typescript
// Complex nested object validation
export const ReservationSchema = z.object({
  id: UUIDSchema,
  guest: GuestSchema,
  rooms: z.array(RoomSchema),
  payments: z.array(PaymentTransactionSchema),
  // ... other fields
});
```

## 🔍 Validation Examples

### Basic Validation

```typescript
import { PropertySchema } from '@/schemas/entities/domains/property';

try {
  const property = PropertySchema.parse({
    id: '123e4567-e89b-12d3-a456-426614174000',
    name: 'Grand Hotel',
    type: 'hotel',
    // ... other fields
  });
  console.log('Valid property:', property);
} catch (error) {
  console.error('Validation failed:', error.errors);
}
```

### Partial Validation

```typescript
import { GuestSchema } from '@/schemas/entities/domains/guest';

// For updates, use partial schemas
const GuestUpdateSchema = GuestSchema.partial();

const updateGuest = GuestUpdateSchema.parse({
  firstName: 'John',
  email: 'john@example.com'
  // Other fields are optional
});
```

### Array Validation

```typescript
import { ReservationSchema } from '@/schemas/entities/domains/reservation';

const ReservationListSchema = z.array(ReservationSchema);

const reservations = ReservationListSchema.parse([
  { /* reservation 1 */ },
  { /* reservation 2 */ },
]);
```

## 🧪 Testing

```typescript
import { describe, it, expect } from 'vitest';
import { PropertySchema } from '@/schemas/entities/domains/property';

describe('Property Schema', () => {
  it('should validate valid property data', () => {
    const validProperty = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      name: 'Test Hotel',
      type: 'hotel',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z',
    };

    expect(() => PropertySchema.parse(validProperty)).not.toThrow();
  });

  it('should reject invalid property data', () => {
    const invalidProperty = {
      id: 'invalid-uuid',
      name: '', // Empty name should fail
      type: 'invalid-type',
    };

    expect(() => PropertySchema.parse(invalidProperty)).toThrow();
  });
});
```

## 🔄 Migration from Monolithic Schema

The modular structure maintains **full backward compatibility**:

```typescript
// Old way (still works)
import { EntitySchemas } from '@/schemas/entities';
const guest = EntitySchemas.Guest.parse(data);

// New way (recommended)
import { GuestSchema } from '@/schemas/entities/domains/guest';
const guest = GuestSchema.parse(data);
```

## 📚 Additional Resources

- [Zod Documentation](https://zod.dev/)
- [TypeScript Schema Validation Best Practices](https://zod.dev/README#best-practices)
- [Domain-Driven Design with TypeScript](https://khalilstemmler.com/articles/domain-driven-design-intro/)
