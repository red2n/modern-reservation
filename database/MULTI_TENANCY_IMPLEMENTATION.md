# Multi-Tenancy Database Implementation

**Version:** 2.0.0
**Date:** October 8, 2025
**Status:** ✅ COMPLETED

## Overview

This document describes the complete multi-tenancy implementation in the Modern Reservation database schema. All changes have been implemented to support tenant-based data isolation, subscription management, and user-tenant associations.

---

## Database Schema Changes

### 1. New ENUMs Added (`01-extensions-and-types.sql`)

#### `tenant_type`
- `CHAIN` - Hotel chain organization
- `INDEPENDENT` - Independent property
- `FRANCHISE` - Franchise operation
- `MANAGEMENT_COMPANY` - Property management company
- `VACATION_RENTAL` - Vacation rental operator

#### `tenant_status`
- `ACTIVE` - Active tenant account
- `SUSPENDED` - Temporarily suspended
- `TRIAL` - Trial period
- `EXPIRED` - Subscription expired
- `CANCELLED` - Account cancelled

#### `subscription_plan`
- `FREE` - Free tier (limited features)
- `STARTER` - Basic paid plan
- `PROFESSIONAL` - Advanced features
- `ENTERPRISE` - Full feature set
- `CUSTOM` - Custom enterprise plan

#### `tenant_role`
- `OWNER` - Tenant owner (full access)
- `ADMIN` - Tenant administrator
- `MANAGER` - Property/operations manager
- `STAFF` - Staff member
- `ACCOUNTANT` - Financial access
- `VIEWER` - Read-only access

#### `billing_cycle`
- `MONTHLY` - Monthly billing
- `QUARTERLY` - Quarterly billing
- `YEARLY` - Annual billing

---

### 2. New Tables Created (`02-core-tables.sql`)

#### `tenants` Table
**Purpose:** Store tenant organizations (hotel chains, franchises, independent properties)

**Key Columns:**
- `id` (UUID, PK) - Unique tenant identifier
- `name` (VARCHAR(200)) - Tenant organization name
- `slug` (VARCHAR(200), UNIQUE) - URL-friendly identifier
- `type` (tenant_type) - Type of organization
- `status` (tenant_status) - Account status
- `email` (VARCHAR(255)) - Primary contact email
- `config` (JSONB) - Tenant configuration settings including:
  - Branding settings (logo, colors)
  - Feature flags (enableMultiProperty, enableChannelManager, etc.)
  - Limits (maxProperties, maxUsers, maxReservationsPerMonth)
  - Localization (defaultCurrency, defaultLanguage, defaultTimezone)
- `subscription` (JSONB) - Subscription information including:
  - Plan details (plan, startDate, endDate, trialEndDate)
  - Billing info (billingCycle, amount, currency)
- `metadata` (JSONB) - Custom fields for flexibility
- Audit fields (created_at, updated_at, created_by, updated_by)
- Soft delete (deleted_at, deleted_by)
- Version control (version)

**Constraints:**
- Slug must match pattern: `^[a-z0-9-]+$`
- Unique slug across all tenants

#### `user_tenant_associations` Table
**Purpose:** Many-to-many relationship between users and tenants

**Key Columns:**
- `id` (UUID, PK) - Association identifier
- `user_id` (UUID) - Reference to user
- `tenant_id` (UUID) - Reference to tenant
- `role` (tenant_role) - User's role within the tenant
- `permissions` (TEXT[]) - Additional granular permissions
- `is_active` (BOOLEAN) - Active status
- `is_primary` (BOOLEAN) - Primary tenant for user (one per user)
- `assigned_by` (UUID) - Who assigned this association
- `assigned_at` (TIMESTAMP) - Assignment timestamp
- `expires_at` (TIMESTAMP) - Optional expiration
- `last_accessed_at` (TIMESTAMP) - Last access timestamp

**Constraints:**
- Unique constraint on (user_id, tenant_id)
- Only one primary tenant per user

---

### 3. Tenant ID Added to Existing Tables

All core business tables now include `tenant_id UUID NOT NULL` for data isolation:

#### `rates`
- **tenant_id** - Tenant owner of rate plans
- Comment: "Multi-tenancy: Tenant owner of this rate"

#### `reservations`
- **tenant_id** - Tenant owner of reservation
- Comment: "Multi-tenancy: Tenant owner of this reservation"

#### `reservation_status_history`
- **tenant_id** - Inherited from reservation
- Comment: "Multi-tenancy: Inherited from reservation"

#### `payments`
- **tenant_id** - Critical for financial isolation
- Comment: "Multi-tenancy: Critical for financial isolation"

#### `availability.room_availability`
- **tenant_id** - Tenant owner of availability data
- Comment: "Multi-tenancy: Tenant owner of availability data"

#### `analytics_metrics`
- **tenant_id** - Tenant owner of analytics data
- Comment: "Multi-tenancy: Tenant owner of analytics data"

#### `analytics_reports`
- **tenant_id** - Tenant owner of report
- Comment: "Multi-tenancy: Tenant owner of report"

---

### 4. Indexes Added (`03-indexes.sql`)

#### Tenant Table Indexes
```sql
-- Core tenant lookups
idx_tenants_slug                    -- Slug lookup (unique identifier)
idx_tenants_type                    -- Filter by tenant type
idx_tenants_status                  -- Filter by status
idx_tenants_active                  -- Active tenants only (WHERE status = 'ACTIVE')
idx_tenants_email                   -- Email lookup
idx_tenants_created_at              -- Sort by creation date
idx_tenants_country                 -- Geographic filtering

-- JSONB indexes for complex queries
idx_tenants_config                  -- GIN index on config JSONB
idx_tenants_subscription            -- GIN index on subscription JSONB

-- Soft delete support
idx_tenants_not_deleted             -- Non-deleted tenants (WHERE deleted_at IS NULL)
```

#### User-Tenant Association Indexes
```sql
idx_user_tenant_user_id             -- All tenants for a user
idx_user_tenant_tenant_id           -- All users for a tenant
idx_user_tenant_role                -- Filter by role
idx_user_tenant_active              -- Active associations only
idx_user_tenant_primary             -- Primary tenant per user
idx_user_tenant_expires             -- Expiring associations
idx_user_tenant_last_accessed       -- Recent access tracking
```

#### Tenant Foreign Key Indexes
All tables with `tenant_id` have composite indexes:
```sql
-- rates
idx_rates_tenant_id
idx_rates_tenant_property

-- reservations
idx_reservations_tenant_id
idx_reservations_tenant_property
idx_reservations_tenant_dates

-- reservation_status_history
idx_res_history_tenant_id

-- payments
idx_payment_tenant_id
idx_payment_tenant_created
idx_payment_tenant_status

-- availability
idx_availability_tenant_id
idx_availability_tenant_property_date

-- analytics_metrics
idx_metric_tenant_id
idx_metric_tenant_type_period

-- analytics_reports
idx_report_tenant_id
idx_report_tenant_type_status
```

**Performance Impact:**
- Fast tenant-scoped queries
- Efficient data isolation enforcement
- Optimized for common multi-tenant query patterns

---

### 5. Foreign Key Constraints (`04-constraints.sql`)

#### Tenant Foreign Keys (ON DELETE RESTRICT)
All business tables reference `tenants.id` with `ON DELETE RESTRICT` to prevent accidental tenant deletion:

```sql
fk_rates_tenant                     -- rates → tenants
fk_reservations_tenant              -- reservations → tenants
fk_res_history_tenant               -- reservation_status_history → tenants
fk_payments_tenant                  -- payments → tenants
fk_availability_tenant              -- room_availability → tenants
fk_metrics_tenant                   -- analytics_metrics → tenants
fk_reports_tenant                   -- analytics_reports → tenants
```

#### User-Tenant Association Constraints
```sql
fk_user_tenant_tenant               -- user_tenant_associations → tenants (ON DELETE CASCADE)
idx_user_tenant_one_primary         -- Unique index ensuring one primary tenant per user
```

**Data Integrity:**
- Cannot delete tenant with existing data (RESTRICT)
- Cascade delete for user associations when tenant is deleted
- Enforces one primary tenant per user

---

### 6. Reference Data & Triggers (`05-reference-data.sql`)

#### Schema Version
```sql
Version 2.0.0 - Multi-tenancy support added - October 2025
```

#### Sample Tenants (Development)

**Tenant 1: Demo Hotel Chain**
```sql
ID:     aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
Type:   CHAIN
Status: ACTIVE
Plan:   ENTERPRISE ($9,999/year)
Features:
  - Multi-property enabled
  - Channel manager enabled
  - Advanced reporting enabled
  - Payment processing enabled
  - Loyalty program enabled
Limits:
  - Max properties: 50
  - Max users: 100
  - Unlimited reservations
```

**Tenant 2: Boutique Beach Resort**
```sql
ID:     bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
Type:   INDEPENDENT
Status: TRIAL
Plan:   STARTER ($49/month, 30-day trial)
Features:
  - Single property only
  - No channel manager
  - Basic reporting
  - Payment processing enabled
Limits:
  - Max properties: 1
  - Max users: 5
  - 100 reservations/month
```

#### Update Triggers
Automatic `updated_at` timestamp updates for:
- `tenants`
- `user_tenant_associations`
- All existing tables (rates, reservations, payments, etc.)

---

## Migration Path

### For Existing Data

If you have existing data without `tenant_id`:

```sql
-- Option 1: Assign all existing data to a default tenant
UPDATE rates SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE reservations SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE payments SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE availability.room_availability SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE analytics_metrics SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE analytics_reports SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
UPDATE reservation_status_history SET tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

-- Option 2: Data migration based on business logic
-- (Requires custom script based on your data structure)
```

### For New Deployments

Run the master schema script:
```bash
psql -U postgres -d modern_reservation -f database/schema/master-schema.sql
```

---

## Application Integration

### GraphQL Resolvers

All resolvers must include tenant context:

```typescript
import { getTenantFromContext, validateTenantAccess } from '@modern-reservation/schemas';

export const reservationResolvers = {
  Query: {
    reservations: async (_, __, context) => {
      const tenantContext = getTenantFromContext(context);
      if (!tenantContext) {
        throw new Error('Tenant context required');
      }

      return await prisma.reservation.findMany({
        where: { tenantId: tenantContext.tenantId }
      });
    }
  },

  Mutation: {
    createReservation: async (_, { input }, context) => {
      const tenantContext = getTenantFromContext(context);

      return await prisma.reservation.create({
        data: {
          ...input,
          tenantId: tenantContext.tenantId
        }
      });
    }
  }
};
```

### Middleware

Add tenant context middleware:

```typescript
app.use((req, res, next) => {
  const tenantId = req.headers['x-tenant-id'];

  if (tenantId) {
    req.tenantContext = {
      tenantId,
      // ... fetch tenant details
    };
  }

  next();
});
```

### Row Level Security (RLS)

Optional PostgreSQL RLS policies:

```sql
-- Enable RLS on tables
ALTER TABLE reservations ENABLE ROW LEVEL SECURITY;

-- Create policy for tenant isolation
CREATE POLICY tenant_isolation_policy ON reservations
  USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

-- Set tenant context per connection
SET app.current_tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
```

---

## Testing

### Verify Multi-Tenancy

```sql
-- Check tenant isolation
SELECT
  t.name as tenant,
  COUNT(DISTINCT r.id) as reservations,
  COUNT(DISTINCT p.id) as payments
FROM tenants t
LEFT JOIN reservations r ON r.tenant_id = t.id
LEFT JOIN payments p ON p.tenant_id = t.id
GROUP BY t.id, t.name;

-- Verify no cross-tenant data access
SELECT * FROM reservations
WHERE tenant_id != 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
```

### Performance Testing

```sql
-- Test tenant-scoped query performance
EXPLAIN ANALYZE
SELECT * FROM reservations
WHERE tenant_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
  AND check_in_date >= CURRENT_DATE;
```

---

## Security Considerations

### Data Isolation
✅ All tables have `tenant_id` column
✅ Foreign key constraints prevent orphaned data
✅ Indexes optimize tenant-scoped queries
✅ ON DELETE RESTRICT prevents accidental deletion

### Access Control
✅ User-tenant associations with roles
✅ Granular permissions support
✅ Expiration dates for temporary access
✅ Primary tenant designation

### Financial Isolation
✅ Payments table has tenant_id (CRITICAL)
✅ Separate gateway configurations per tenant
✅ Subscription billing tracked per tenant

---

## Monitoring & Maintenance

### Key Metrics to Monitor

```sql
-- Active tenants
SELECT COUNT(*) FROM tenants WHERE status = 'ACTIVE';

-- Trial tenants expiring soon
SELECT name, subscription->>'trialEndDate'
FROM tenants
WHERE status = 'TRIAL'
  AND (subscription->>'trialEndDate')::date <= CURRENT_DATE + INTERVAL '7 days';

-- Tenant data growth
SELECT
  t.name,
  COUNT(r.id) as total_reservations,
  SUM(p.amount) as total_revenue
FROM tenants t
LEFT JOIN reservations r ON r.tenant_id = t.id
LEFT JOIN payments p ON p.tenant_id = t.id
GROUP BY t.id, t.name
ORDER BY total_revenue DESC;
```

### Regular Maintenance

```sql
-- Vacuum and analyze tenant tables
VACUUM ANALYZE tenants;
VACUUM ANALYZE user_tenant_associations;

-- Reindex for performance
REINDEX TABLE tenants;
REINDEX TABLE user_tenant_associations;
```

---

## Feature Flags

Tenants can have the following features enabled/disabled via `config` JSONB:

- `enableMultiProperty` - Manage multiple properties
- `enableChannelManager` - OTA integrations
- `enableAdvancedReporting` - Advanced analytics
- `enablePaymentProcessing` - Online payments
- `enableLoyaltyProgram` - Guest loyalty features
- `brandingEnabled` - Custom branding

Example query:
```sql
SELECT name
FROM tenants
WHERE config->>'enableChannelManager' = 'true';
```

---

## Subscription Limits

Enforced via `config` JSONB:

- `maxProperties` - Maximum number of properties
- `maxUsers` - Maximum number of staff users
- `maxReservationsPerMonth` - Monthly reservation limit

Check limits:
```sql
SELECT
  name,
  (config->>'maxProperties')::int as max_properties,
  (config->>'maxUsers')::int as max_users
FROM tenants;
```

---

## Rollback Procedure

To rollback multi-tenancy changes:

```sql
-- 1. Drop foreign key constraints
ALTER TABLE rates DROP CONSTRAINT fk_rates_tenant;
-- ... repeat for all tables

-- 2. Drop tenant columns
ALTER TABLE rates DROP COLUMN tenant_id;
-- ... repeat for all tables

-- 3. Drop tenant tables
DROP TABLE user_tenant_associations;
DROP TABLE tenants;

-- 4. Drop tenant enums
DROP TYPE tenant_role;
DROP TYPE billing_cycle;
DROP TYPE subscription_plan;
DROP TYPE tenant_status;
DROP TYPE tenant_type;
```

**⚠️ WARNING:** This will result in data loss. Backup database before rollback!

---

## Summary

✅ **8/8 Tasks Completed**

1. ✅ Added 5 new ENUMs for tenant management
2. ✅ Created `tenants` table with JSONB config and subscription
3. ✅ Created `user_tenant_associations` for many-to-many relationships
4. ✅ Added `tenant_id` to 7 existing tables
5. ✅ Created 50+ indexes for optimal performance
6. ✅ Added foreign key constraints with proper cascade rules
7. ✅ Inserted sample tenant data and update triggers
8. ✅ Updated master schema with tenant information

**Database Version:** 2.0.0
**Total Tables:** 11 (was 9)
**New Enums:** 5
**New Indexes:** 50+
**Sample Tenants:** 2

---

## Next Steps

1. **Backend Integration:**
   - Update Spring Boot entities with `@Column(name = "tenant_id")`
   - Add tenant context to GraphQL resolvers
   - Implement tenant middleware

2. **Frontend Integration:**
   - Build tenant selector UI component
   - Add tenant context to Apollo Client
   - Create tenant admin dashboard

3. **Testing:**
   - Write integration tests for tenant isolation
   - Test subscription limits enforcement
   - Verify cross-tenant data protection

4. **Production Deployment:**
   - Run migration scripts
   - Assign existing data to default tenant
   - Enable monitoring and alerting

---

**Questions or Issues?**
Refer to the Zod schemas in `libs/shared/schemas/src/entities/domains/tenant.ts`
and helper utilities in `libs/shared/schemas/src/utils/tenant-helpers.ts`

**End of Document**
