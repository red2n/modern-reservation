# Modern Reservation - Core Principles

## 🎯 Project Philosophy

The Modern Reservation system follows these core architectural principles:

### 1. **Shared First**
Always use shared libraries instead of duplicating code:
- `@modern-reservation/schemas` - Data validation and types
- `@modern-reservation/graphql-client` - GraphQL operations and hooks
- `@modern-reservation/ui-components` - Reusable UI components

### 2. **Convention Over Configuration**
Follow established patterns:
- UPPERCASE enum values (matches PostgreSQL)
- Single quotes in TypeScript/JavaScript
- snake_case in database, camelCase in code
- `file:` protocol for monorepo dependencies

### 3. **SOLID Principles**
Strictly follow SOLID design principles:
- **S**ingle Responsibility - One class, one purpose
- **O**pen/Closed - Open for extension, closed for modification
- **L**iskov Substitution - Subtypes must be substitutable
- **I**nterface Segregation - Many specific interfaces over one general
- **D**ependency Inversion - Depend on abstractions, not concretions

### 4. **Consistency**
- Use Biome.js for all Node.js projects (not ESLint/Prettier)
- Use `./dev.sh` for all service operations
- Use shared tooling across the monorepo

## 🚫 Anti-Patterns to Avoid

### Never Do:
1. ❌ Create local schema definitions - use `@modern-reservation/schemas`
2. ❌ Create separate Apollo clients - use `@modern-reservation/graphql-client`
3. ❌ Use lowercase enum values - always UPPERCASE
4. ❌ Use `npm link` or `workspace:*` - use `file:` protocol
5. ❌ Run services with `docker-compose` directly - use `./dev.sh`
6. ❌ Use ESLint or Prettier - use Biome.js
7. ❌ Create scattered README files - update existing docs

### Always Do:
1. ✅ Import from shared packages
2. ✅ Use UPPERCASE enums: `'PENDING'`, `'CONFIRMED'`, `'COMPLETED'`
3. ✅ Use `file:` protocol: `"@modern-reservation/schemas": "file:../../../libs/shared/schemas"`
4. ✅ Use `./dev.sh` commands: `start`, `stop`, `status`, `clean`
5. ✅ Use Biome.js: `npx biome check --write .`
6. ✅ Update existing documentation in `docs/`

## 📐 Architecture Decisions

### Monorepo Structure
```
modern-reservation/
├── apps/
│   ├── backend/
│   │   ├── java-services/    # Spring Boot microservices
│   │   └── node-services/    # Node.js services
│   └── frontend/
│       └── guest-portal/     # Next.js application
├── libs/
│   └── shared/               # Shared libraries
│       ├── schemas/          # Zod schemas + types
│       ├── graphql-client/   # Apollo client + operations
│       └── ui-components/    # React components
└── infrastructure/           # Docker, Kafka, PostgreSQL
```

### Technology Stack
- **Frontend**: Next.js 15, Tailwind CSS v4, Apollo Client
- **Backend**: Spring Boot (Java 21), Node.js 20, GraphQL
- **Database**: PostgreSQL 16 with multi-tenancy
- **Message Queue**: Apache Kafka with Avro schemas
- **Cache**: Redis
- **Tools**: Biome.js, Docker, Maven

### Database Design
- Multi-tenant architecture with tenant isolation
- UPPERCASE enums in PostgreSQL
- snake_case column names
- Audit logging for all changes
- Soft deletes with `deleted_at` timestamp

## 🔧 Tool Preferences

### Code Search
```bash
# ✅ CORRECT: Use ripgrep
rg "searchTerm" --type typescript
rg -i "case-insensitive" -t java

# ❌ WRONG: Don't use grep
grep -r "searchTerm" .
```

### Code Formatting
```bash
# ✅ CORRECT: Use Biome.js
npx biome check .
npx biome check --write --unsafe .
./dev.sh biome-fix

# ❌ WRONG: Don't use ESLint/Prettier
eslint --fix .
prettier --write .
```

### Service Management
```bash
# ✅ CORRECT: Use dev.sh
./dev.sh start
./dev.sh stop
./dev.sh status
./dev.sh logs api-gateway

# ❌ WRONG: Don't use direct commands
docker-compose up
mvn spring-boot:run
npm run dev
```

## 📝 Code Style

### TypeScript/JavaScript
```typescript
// ✅ CORRECT
import { ReservationSchema } from '@modern-reservation/schemas';
import { useGetPropertiesQuery } from '@modern-reservation/graphql-client';

const status = 'PENDING'; // UPPERCASE enum
const config = {
  indent: 2,
  quotes: 'single',
  semicolons: true,
};

// ❌ WRONG
import { z } from 'zod';
const LocalSchema = z.object({ ... }); // Don't create local schemas
const status = "pending"; // Don't use lowercase enums
```

### Java
```java
// ✅ CORRECT: Follow SOLID principles
@Service
public class ReservationService {
    private final ReservationRepository repository;

    @Autowired
    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }
}
```

## 🎓 Learning Resources

- [Project Documentation](../../docs/README.md)
- [Development Guide](../../docs/guides/DEV_QUICK_REFERENCE.md)
- [VS Code Setup](../../docs/VSCODE_CONFIGURATION_COMPLETE.md)
- [Biome.js Docs](https://biomejs.dev/)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Next.js Docs](https://nextjs.org/docs)
