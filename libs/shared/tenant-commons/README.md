# Tenant Commons Library

Shared library for multi-tenancy support across all Modern Reservation microservices.

## Purpose

This library provides common utilities, DTOs, and enums used by all services in the microservices architecture. It does **NOT** contain any database entities or business logic - only shared code.

## What's Included

### 1. `TenantContext`
Thread-local storage for current tenant ID extracted from JWT token.

```java
// Set tenant (in filter/interceptor)
TenantContext.setCurrentTenantId(tenantId);

// Get tenant (in service)
UUID tenantId = TenantContext.getCurrentTenantId();

// Clear tenant (in finally block)
TenantContext.clear();
```

### 2. Enums
- `TenantType`: CHAIN, INDEPENDENT, FRANCHISE, MANAGEMENT_COMPANY, VACATION_RENTAL
- `TenantStatus`: ACTIVE, SUSPENDED, TRIAL, EXPIRED, CANCELLED

### 3. Kafka Event DTOs
- `TenantEvent`: Base event DTO for tenant changes
- Event types: TENANT_CREATED, TENANT_UPDATED, TENANT_DELETED, TENANT_SUSPENDED, TENANT_ACTIVATED

### 4. Cache DTO
- `TenantCacheDTO`: Lightweight DTO for local tenant caching in business services

### 5. Annotations
- `@RequiresTenantContext`: Mark methods that require tenant context

## Usage in Services

### Add Dependency

```xml
<dependency>
    <groupId>com.modernreservation</groupId>
    <artifactId>tenant-commons</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Example: Using in Business Service

```java
@Service
public class ReservationService {

    @RequiresTenantContext
    public Reservation createReservation(ReservationRequest request) {
        // Get tenant from context
        UUID tenantId = TenantContext.getCurrentTenantId();

        // Use tenant ID for data isolation
        Reservation reservation = new Reservation();
        reservation.setTenantId(tenantId);
        // ...
    }
}
```

### Example: Kafka Event Consumer

```java
@KafkaListener(topics = "tenant.created", groupId = "reservation-service")
public void handleTenantCreated(TenantEvent event) {
    // Update local tenant cache
    TenantCache cache = new TenantCache();
    cache.setTenantId(event.getTenantId());
    cache.setName(event.getName());
    cache.setStatus(event.getStatus());
    cache.setLastSyncedAt(LocalDateTime.now());

    tenantCacheRepository.save(cache);
}
```

## Architecture

```
┌──────────────────────────────────────────────────┐
│         tenant-commons (Shared Library)          │
│                                                   │
│  - TenantContext (Thread-local)                  │
│  - TenantType, TenantStatus (Enums)              │
│  - TenantEvent (Kafka DTO)                       │
│  - TenantCacheDTO (Cache model)                  │
│  - @RequiresTenantContext (Annotation)           │
│                                                   │
│  ❌ NO database entities                         │
│  ❌ NO business logic                            │
│  ✅ Only shared utilities                        │
└──────────────────────────────────────────────────┘
                      │
                      │ Used by
                      ▼
    ┌─────────────────────────────────────────┐
    │                                          │
    ▼                ▼                ▼        ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ Tenant   │  │Reserv-   │  │  Rate    │  │ Payment  │
│ Service  │  │ation     │  │  Mgmt    │  │Processor │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

## Key Principles

1. **No Business Logic**: This library contains only shared utilities
2. **No Database Entities**: Each service owns its database entities
3. **Event-Driven**: Services communicate via Kafka events
4. **Thread-Safe**: TenantContext uses ThreadLocal for isolation
5. **Lightweight**: Minimal dependencies

## Building

```bash
cd libs/shared/tenant-commons
mvn clean install
```

This installs the library to your local Maven repository for use by other services.

## Version

Current version: **1.0.0**

## Related Documentation

- [Multi-Tenancy Guide](../../../docs/MULTI_TENANCY.md)
- [Architecture Overview](../../../docs/architecture/)
