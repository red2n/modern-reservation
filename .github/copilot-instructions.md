# Modern Reservation Project - Copilot Instructions

## Core Principles

You are helping with the Modern Reservation project. Always follow these guidelines:

### 🚫 Never Do This:
1. **DO NOT** create unnecessary README.md files in subdirectories
2. **DO NOT** use `npm link` for monorepo dependencies
3. **DO NOT** create local schema definitions
4. **DO NOT** create separate Apollo/GraphQL clients in each app
5. **DO NOT** use lowercase enum values

### ✅ Always Do This:
1. **USE** shared schemas from `@modern-reservation/schemas`
2. **USE** shared GraphQL client from `@modern-reservation/graphql-client`
3. **USE** `file:` protocol for monorepo dependencies in package.json
4. **USE** UPPERCASE enum values to match PostgreSQL (e.g., 'PENDING', 'CONFIRMED')
5. **FOLLOW** SOLID principles strictly
6. **UPDATE** existing documentation instead of creating new files

## Schemas

When working with data validation or types:

```typescript
// ✅ CORRECT
import { ReservationSchema, PaymentSchema } from '@modern-reservation/schemas';

// ❌ WRONG - Don't create local schemas
const LocalSchema = z.object({ ... });
```

## GraphQL

When making API calls:

```typescript
// ✅ CORRECT
import { useGetPropertiesQuery, useCreateReservationMutation } from '@modern-reservation/graphql-client';

// ❌ WRONG - Don't create separate clients
const client = new ApolloClient({ ... });
```

## Enums

All enum values must be UPPERCASE to match PostgreSQL:

```typescript
// ✅ CORRECT
export const StatusSchema = z.enum(['PENDING', 'CONFIRMED', 'COMPLETED']);

// ❌ WRONG
export const StatusSchema = z.enum(['pending', 'confirmed']);
```

## Monorepo Dependencies

In package.json:

```json
// ✅ CORRECT
{
  "dependencies": {
    "@modern-reservation/schemas": "file:../../../libs/shared/schemas"
  }
}

// ❌ WRONG
{
  "dependencies": {
    "@modern-reservation/schemas": "workspace:*"
  }
}
```

## Documentation

- Technical decisions → `docs/adr/` (Architecture Decision Records)
- API docs → `docs/api/`
- **DO NOT** create scattered README files
- Update existing documentation instead

## Architecture

- Frontend: `apps/frontend/*` (Next.js without Turbopack)
- Backend: `apps/backend/java-services/*` (Spring Boot)
- Shared: `libs/shared/*`
- Database: PostgreSQL with UPPERCASE enums

## When Suggesting Code

1. Check if shared schemas exist before creating new ones
2. Check if GraphQL operations exist in shared client
3. Verify enum values are UPPERCASE
4. Use file: protocol for internal dependencies
5. Don't suggest creating README files unless specifically requested
6. If a Linux command is missing, recommend: `sudo apt install <package-name>`

## System Packages

When encountering "command not found" errors:
- Recommend exact installation command: `sudo apt install <package>`
- Common packages: tree, curl, wget, git, jq, make, build-essential
- Example: "Command 'tree' not found" → suggest `sudo apt install tree`

## Script Management

**ALWAYS use `./dev.sh` script for application operations:**

```bash
# ✅ CORRECT: Use dev.sh
./dev.sh start           # Start all services
./dev.sh stop            # Stop all services
./dev.sh status          # Check status
./dev.sh clean           # Clean restart
./dev.sh docker-start    # Start Docker
./dev.sh logs <service>  # View logs

# ❌ WRONG: Don't use direct commands
docker-compose up
mvn spring-boot:run
java -jar service.jar
```

**Creating New Scripts:**
1. Create in `scripts/` folder with kebab-case naming (e.g., `check-health.sh`)
2. Make executable: `chmod +x scripts/my-script.sh`
3. Add to `dev.sh` case statement
4. Update help section in `dev.sh`

## MCP Server

Query the MCP server for:
- `get_project_rules` - Get specific rules by category
- `get_code_patterns` - Get correct code patterns
- `validate_approach` - Validate if an approach follows guidelines
- `get_architecture_decisions` - Understand why decisions were made
