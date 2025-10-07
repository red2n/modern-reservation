# Modern Reservation MCP Server

This MCP (Model Context Protocol) server provides project-specific rules, guidelines, and conventions to help AI assistants (like GitHub Copilot) understand and follow Modern Reservation project standards.

## What It Does

The MCP server provides:
- **Project Rules**: Guidelines for schemas, GraphQL, architecture, database, dependencies, etc.
- **Code Patterns**: Correct vs incorrect code examples
- **Architecture Decisions**: Why certain choices were made
- **Approach Validation**: Checks if a proposed solution follows project guidelines

## Quick Start

### 1. Install Dependencies

```bash
cd tools/mcp-server
npm install
```

### 2. Build the Server

```bash
npm run build
```

### 3. Configure in VS Code

Add to your VS Code settings (`.vscode/settings.json`):

```json
{
  "github.copilot.chat.codeGeneration.instructions": [
    {
      "text": "Use the modern-reservation MCP server for project guidelines"
    }
  ],
  "mcp.servers": {
    "modern-reservation": {
      "command": "node",
      "args": [
        "/home/subramani/modern-reservation/tools/mcp-server/dist/index.js"
      ]
    }
  }
}
```

### 4. Configure in Claude Desktop (Alternative)

Add to `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) or `%APPDATA%/Claude/claude_desktop_config.json` (Windows):

```json
{
  "mcpServers": {
    "modern-reservation": {
      "command": "node",
      "args": [
        "/home/subramani/modern-reservation/tools/mcp-server/dist/index.js"
      ]
    }
  }
}
```

## Available Tools

### 1. `get_project_rules`

Get project-specific rules by category.

**Parameters:**
- `category` (optional): "general", "schemas", "graphql", "architecture", "database", "dependencies", "documentation", "buildTools", or "all"

**Example:**
```typescript
// Get all schema-related rules
get_project_rules({ category: "schemas" })
```

### 2. `get_code_patterns`

Get correct code patterns and anti-patterns.

**Parameters:**
- `pattern` (optional): "importingSchemas", "importingGraphQL", "monorepoPackage", "enumValues", or "all"

**Example:**
```typescript
// Get schema import patterns
get_code_patterns({ pattern: "importingSchemas" })
```

### 3. `get_architecture_decisions`

Get architecture decisions and their reasoning.

**Parameters:**
- `decision` (optional): Specific decision name or "all"

**Example:**
```typescript
// Get all architecture decisions
get_architecture_decisions({ decision: "all" })
```

### 4. `validate_approach`

Validate if a proposed approach follows project guidelines.

**Parameters:**
- `description` (required): Description of the approach to validate

**Example:**
```typescript
// Validate an approach
validate_approach({
  description: "I want to create a new Apollo client in the admin portal"
})

// Response will indicate violations and recommendations
```

## Available Prompts

### 1. `project_guidelines`

Get a comprehensive overview of all project guidelines.

### 2. `schema_best_practices`

Get best practices for working with schemas.

### 3. `monorepo_setup`

Get guidance on monorepo setup and dependency management.

## Key Rules Enforced

### ❌ Don't Do This:
- Create unnecessary README.md files in subdirectories
- Use npm link for monorepo dependencies
- Create local schema definitions
- Create separate Apollo/GraphQL clients
- Use lowercase enum values

### ✅ Do This Instead:
- Use shared schemas from `@modern-reservation/schemas`
- Use shared GraphQL client from `@modern-reservation/graphql-client`
- Use `file:` protocol for monorepo dependencies
- Use UPPERCASE enum values to match PostgreSQL
- Follow SOLID principles
- Update existing documentation instead of creating new files

## Development

```bash
# Watch mode for development
npm run watch

# Build
npm run build

# Run directly
npm start
```

## How Copilot Uses This

When you ask Copilot to help with code in this project, it can:

1. **Check rules before generating code**
   ```
   "Create a reservation form"
   → Copilot checks: Should I use shared schemas? ✅ Yes!
   → Uses @modern-reservation/schemas
   ```

2. **Validate your approaches**
   ```
   "Can I create a new GraphQL client?"
   → MCP validates: ❌ No, use shared client
   → Suggests: @modern-reservation/graphql-client
   ```

3. **Follow architecture patterns**
   ```
   "Add a new enum"
   → Checks: Must be UPPERCASE ✅
   → Generates: 'PENDING', 'CONFIRMED', etc.
   ```

## Examples

### Example 1: Asking Copilot for Help

**You:** "Create a payment form component"

**Copilot (with MCP):**
```typescript
// ✅ Uses shared schemas
import { PaymentSchema, PaymentMethodTypeSchema } from '@modern-reservation/schemas';
import { useCreatePaymentMutation } from '@modern-reservation/graphql-client';

// Correct: UPPERCASE enums from shared schemas
const paymentMethods = PaymentMethodTypeSchema.options;
```

### Example 2: Validation

**You:** "I want to create a README in the component folder"

**MCP Response:**
```json
{
  "valid": false,
  "violations": [
    "❌ Do not create unnecessary README files"
  ],
  "recommendations": [
    "✅ Update existing README.md or add to docs/ directory"
  ]
}
```

## Troubleshooting

### Server not responding
```bash
# Check if built
ls -la tools/mcp-server/dist/

# Rebuild
cd tools/mcp-server && npm run build
```

### VS Code not recognizing server
- Restart VS Code after configuration
- Check the output panel for MCP errors
- Verify the path in settings.json is correct

### Changes not taking effect
```bash
# Rebuild after changes
npm run build

# Restart VS Code or reload window
```

## Contributing

To add new rules or patterns:

1. Edit `src/index.ts`
2. Add rules to `PROJECT_RULES`, `COMMON_PATTERNS`, or `ARCHITECTURE_DECISIONS`
3. Rebuild: `npm run build`
4. Restart your editor

## License

MIT
