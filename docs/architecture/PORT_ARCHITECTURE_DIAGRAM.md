# Port Configuration Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    PORT REGISTRY (Single Source of Truth)               │
│                  libs/shared/port-manager/src/port-registry.ts          │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  Services: 35+                                                  │    │
│  │  Categories: Frontend, Node.js, Java, Infrastructure           │    │
│  │  Security: Internal/External classification                     │    │
│  │  Conflict Detection: Automatic validation                       │    │
│  └────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ↓
                        ./dev.sh config-generate
                                    │
                  ┌─────────────────┴─────────────────┐
                  │    CONFIG GENERATOR               │
                  │    (ConfigGenerator class)        │
                  └─────────────────┬─────────────────┘
                                    │
        ┌───────────────┬───────────┴──────────┬──────────────┬────────────┐
        ↓               ↓                      ↓              ↓            ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Java       │ │   Node.js    │ │  TypeScript  │ │    Java      │ │ Kubernetes   │
│   Services   │ │   Services   │ │  Constants   │ │  Constants   │ │  ConfigMap   │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
        │               │                      │              │            │
        ↓               ↓                      ↓              ↓            ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│application-  │ │   .env.ports │ │   ports.ts   │ │ServicePorts  │ │configmap-    │
│ports.yml     │ │              │ │              │ │.java         │ │ports.yaml    │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
        │               │                      │              │            │
        ↓               ↓                      ↓              ↓            ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Reservation  │ │     Auth     │ │   Frontend   │ │   Business   │ │     K8s      │
│   Engine     │ │   Service    │ │     Apps     │ │   Services   │ │     Pods     │
│              │ │              │ │              │ │              │ │              │
│ Port: 8100   │ │ Port: 3100   │ │ Port: 3000   │ │ Uses const   │ │ Uses env     │
│ Internal ✓   │ │ Internal ✓   │ │ External ✓   │ │ values       │ │ from CM      │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

## Configuration Flow

### Java Service (Spring Boot)

```
Port Registry → ConfigGenerator → application-ports.yml → application.yml → Service
    8100                            server.port: 8100      ${server.port}     :8100
```

**Example: reservation-engine**
```yaml
# application-ports.yml (generated)
server:
  port: 8100
  address: 127.0.0.1

# application.yml (manual)
spring:
  config:
    import: "optional:file:./application-ports.yml"
server:
  port: ${server.port}  # Uses 8100
```

### Node.js Service

```
Port Registry → ConfigGenerator → .env.ports → index.ts → Service
    3100                          PORT=3100    process.env.PORT   :3100
```

**Example: auth-service**
```bash
# .env.ports (generated)
PORT=3100
HOST=127.0.0.1
```

```typescript
// index.ts (manual)
config({ path: '../.env.ports' });
const PORT = Number(process.env.PORT);  // 3100
```

## Service Communication

```
┌────────────────────────────────────────────────────────────────────┐
│                         DOCKER NETWORK                              │
│                                                                     │
│  ┌──────────────┐                                                  │
│  │   Gateway    │ ← External Access (0.0.0.0:8080)                │
│  │   :8080      │                                                  │
│  └──────┬───────┘                                                  │
│         │                                                           │
│         ├──→ Reservation Engine :8100 (127.0.0.1 - Internal)      │
│         ├──→ Auth Service :3100 (127.0.0.1 - Internal)            │
│         ├──→ Rate Management :8120 (127.0.0.1 - Internal)         │
│         └──→ Payment Processor :8130 (127.0.0.1 - Internal)       │
│                                                                     │
│  ┌──────────────┐                                                  │
│  │  Postgres    │ ← Internal Only (:5432)                         │
│  └──────────────┘                                                  │
│                                                                     │
│  ┌──────────────┐                                                  │
│  │    Redis     │ ← Internal Only (:6379)                         │
│  └──────────────┘                                                  │
│                                                                     │
│  ┌──────────────┐                                                  │
│  │    Kafka     │ ← Internal Only (:9092)                         │
│  └──────────────┘                                                  │
└────────────────────────────────────────────────────────────────────┘
         ↑                                       ↑
    External Access                        No External Access
    (Gateway Only)                         (All Backend Services)
```

## Port Categories

```
┌──────────────────────────────────────────────────────────────────────┐
│                         PORT RANGES                                  │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Frontend           3000-3099    Guest, Admin, Staff Portals        │
│                                                                      │
│  Node.js Services   3100-3199    Auth, Notification, WebSocket      │
│                                                                      │
│  Databases          5432-5449    PostgreSQL                         │
│                                                                      │
│  Cache              6379-6399    Redis                              │
│                                                                      │
│  Gateway            8080-8099    Spring Cloud Gateway               │
│                                                                      │
│  Java Business      8100-8199    Reservation, Rate, Payment         │
│                                                                      │
│  Service Discovery  8761-8799    Eureka                             │
│                                                                      │
│  Config Server      8888-8899    Spring Config                      │
│                                                                      │
│  Monitoring         9090-9099    Prometheus                         │
│                                                                      │
│  Message Queue      9092         Kafka                              │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

## Update Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│  Developer Updates Port Registry                                    │
│  (Add/Change/Remove service)                                        │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│  ./dev.sh config-generate                                           │
│  (Automatic generation)                                             │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ↓                   ↓                   ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Update     │    │   Update     │    │   Update     │
│   Java       │    │   Node.js    │    │ TypeScript/  │
│   Configs    │    │   Configs    │    │   Java       │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────────┐
│  ./dev.sh build                                                     │
│  (Rebuild services)                                                 │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│  ./dev.sh clean                                                     │
│  (Restart with new configuration)                                   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│  All Services Running with Correct Ports ✓                          │
└─────────────────────────────────────────────────────────────────────┘
```

## Security Model

```
┌────────────────────────────────────────────────────────────────────┐
│                    EXTERNAL (Internet)                              │
└────────────────────────────┬───────────────────────────────────────┘
                             │
                             ↓
                   ┌─────────────────┐
                   │   Firewall      │
                   │   Only :8080    │
                   └────────┬────────┘
                            │
                            ↓
┌────────────────────────────────────────────────────────────────────┐
│                    GATEWAY NETWORK                                  │
│                                                                     │
│   ┌─────────────────┐              ┌─────────────────┐            │
│   │   Gateway       │              │  Guest Portal   │            │
│   │   :8080         │              │  :3000          │            │
│   │   (0.0.0.0)     │              │  (0.0.0.0)      │            │
│   └────────┬────────┘              └─────────────────┘            │
└────────────┼────────────────────────────────────────────────────────┘
             │
             ↓ (Internal Network Bridge)
┌────────────────────────────────────────────────────────────────────┐
│                    BACKEND INTERNAL NETWORK                         │
│                    (internal: true - No External Access)            │
│                                                                     │
│   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐        │
│   │ Auth Service  │  │ Reservation   │  │ Rate Mgmt     │        │
│   │ :3100         │  │ Engine :8100  │  │ :8120         │        │
│   │ (127.0.0.1)   │  │ (127.0.0.1)   │  │ (127.0.0.1)   │        │
│   └───────────────┘  └───────────────┘  └───────────────┘        │
│                                                                     │
│   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐        │
│   │ Config Server │  │ Eureka Server │  │ Postgres      │        │
│   │ :8888         │  │ :8761         │  │ :5432         │        │
│   │ (127.0.0.1)   │  │ (127.0.0.1)   │  │ (127.0.0.1)   │        │
│   └───────────────┘  └───────────────┘  └───────────────┘        │
└────────────────────────────────────────────────────────────────────┘
         ↑
   No External Access - All services isolated
```

## Type Safety

```typescript
// TypeScript (Frontend)
import { ServicePorts, ServiceUrls } from '@modern-reservation/schemas/ports';

const apiUrl = ServiceUrls.GATEWAY_SERVICE;  // Type-safe!
const authPort = ServicePorts.AUTH_SERVICE;   // Auto-complete works!
```

```java
// Java (Backend)
import com.modernreservation.config.ServicePorts;

int gatewayPort = ServicePorts.GATEWAY_SERVICE;  // Compile-time constant
String url = "http://localhost:" + ServicePorts.RESERVATION_ENGINE;
```

## Validation Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│  Developer Changes Port Registry                                    │
└───────────────────────────┬─────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│  ./dev.sh port-validate                                             │
│  ├─ Check for duplicate ports                                       │
│  ├─ Validate port ranges                                            │
│  └─ Ensure no conflicts                                             │
└───────────────────────────┬─────────────────────────────────────────┘
                            ↓
                    ┌───────────────┐
                    │  Conflicts?   │
                    └───┬───────┬───┘
                        │       │
                   Yes  │       │  No
                        ↓       ↓
              ┌─────────────┐   ┌─────────────┐
              │   ❌ FAIL   │   │ ✅ PASS     │
              │   Fix them  │   │ Continue    │
              └─────────────┘   └──────┬──────┘
                                       ↓
                        ┌─────────────────────────────┐
                        │ ./dev.sh config-generate    │
                        │ (Safe to generate)          │
                        └─────────────────────────────┘
```

## Benefits Summary

```
┌──────────────────────────────────────────────────────────────────────┐
│                     BEFORE                 │         AFTER           │
├──────────────────────────────────────────────────────────────────────┤
│                                            │                         │
│  ❌ 30+ files with port configs           │  ✅ 1 source of truth  │
│  ❌ Manual synchronization                 │  ✅ Auto-generated      │
│  ❌ Frequent conflicts                     │  ✅ Conflict detection  │
│  ❌ No type safety                         │  ✅ Type-safe constants │
│  ❌ Different formats per service          │  ✅ Consistent format   │
│  ❌ Error-prone updates                    │  ✅ 1 command updates   │
│  ❌ No validation                          │  ✅ Automatic validation│
│                                            │                         │
└──────────────────────────────────────────────────────────────────────┘
```

---

**Status:** ✅ PRODUCTION-READY
**Maintenance:** Single command: `./dev.sh config-generate`
**Validation:** `./dev.sh port-validate`
**Security:** Automatic internal/external classification
