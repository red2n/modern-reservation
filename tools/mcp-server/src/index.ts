#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  ListPromptsRequestSchema,
  GetPromptRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

/**
 * Modern Reservation MCP Server
 *
 * This server provides project-specific rules, guidelines, and conventions
 * to help AI assistants (like Copilot) understand and follow project standards.
 */

// Project Rules and Guidelines
const PROJECT_RULES = {
  general: [
    "NEVER create unnecessary README.md files in subdirectories",
    "Use existing README.md files at the root level only",
    "Follow SOLID principles strictly",
    "Use TypeScript for all frontend code",
    "Use Java (Spring Boot) for backend services",
  ],

  schemas: [
    "ALWAYS use shared schemas from libs/shared/schemas",
    "Import schemas from @modern-reservation/schemas package",
    "DO NOT duplicate schema definitions across services",
    "All enum values must be UPPERCASE to match PostgreSQL",
    "Use Zod for TypeScript schema validation",
  ],

  graphql: [
    "Use shared GraphQL client from libs/shared/graphql-client",
    "Import from @modern-reservation/graphql-client package",
    "DO NOT create separate GraphQL clients in each app",
    "All GraphQL operations must be defined in shared package",
  ],

  architecture: [
    "Monorepo structure using file: protocol for dependencies",
    "Frontend apps in apps/frontend/*",
    "Backend services in apps/backend/java-services/*",
    "Shared libraries in libs/shared/*",
    "Keep business logic in backend services",
    "Frontend should only handle UI and state management",
  ],

  database: [
    "PostgreSQL is the primary database",
    "All database enums must be UPPERCASE",
    "Schema files in database/schema/",
    "Use Flyway for migrations",
    "Never bypass schema validation",
  ],

  dependencies: [
    "Use file: protocol for internal monorepo dependencies",
    "DO NOT use npm link",
    "Add transpilePackages in next.config.ts for shared packages",
    "Keep dependency versions synchronized across apps",
  ],

  documentation: [
    "Technical decisions in docs/adr/ (Architecture Decision Records)",
    "API documentation in docs/api/",
    "DO NOT create scattered README files",
    "Update existing documentation instead of creating new files",
  ],

  buildTools: [
    "Next.js for frontend apps (without Turbopack)",
    "Use Webpack bundler for Next.js",
    "Maven for Java services",
    "TypeScript for type checking",
  ],

  systemPackages: [
    "If a Linux command is missing, recommend: sudo apt install <package-name>",
    "Common packages: tree, curl, wget, git, jq, make",
    "Always provide the exact apt install command",
    "Example: 'Command tree not found' → recommend 'sudo apt install tree'",
    "Check if running on Ubuntu/Debian before suggesting apt",
  ],

  scriptManagement: [
    "ALWAYS use ./dev.sh script for starting/stopping the application",
    "NEVER suggest direct docker or java commands, use dev.sh instead",
    "Available dev.sh commands: start, stop, status, clean, docker-start, logs, ui-*",
    "If a new operation is needed, suggest creating a new script in scripts/ folder",
    "New scripts should be added to scripts/ and integrated into dev.sh",
    "Script naming: use kebab-case (e.g., check-health.sh, start-services.sh)",
    "All scripts should be executable (chmod +x) and have #!/bin/bash shebang",
    "Integration: Add new command to dev.sh case statement with appropriate routing",
  ],
};

const COMMON_PATTERNS = {
  importingSchemas: `
// ✅ CORRECT: Import from shared schemas
import { ReservationSchema, PaymentSchema } from '@modern-reservation/schemas';

// ❌ WRONG: Don't create local schemas
const LocalReservationSchema = z.object({ ... });
`,

  importingGraphQL: `
// ✅ CORRECT: Import from shared GraphQL client
import { useGetPropertiesQuery } from '@modern-reservation/graphql-client';

// ❌ WRONG: Don't create separate Apollo clients
const client = new ApolloClient({ ... });
`,

  monorepoPackage: `
// ✅ CORRECT: Use file: protocol in package.json
{
  "dependencies": {
    "@modern-reservation/schemas": "file:../../../libs/shared/schemas"
  }
}

// ❌ WRONG: Don't use npm link or workspace:*
`,

  enumValues: `
// ✅ CORRECT: UPPERCASE enum values matching DB
export const ReservationStatusSchema = z.enum([
  'PENDING',
  'CONFIRMED',
  'CHECKED_IN'
]);

// ❌ WRONG: lowercase enums
export const ReservationStatusSchema = z.enum([
  'pending',
  'confirmed'
]);
`,

  usingDevScript: `
// ✅ CORRECT: Use dev.sh for all operations
./dev.sh start           # Start all services
./dev.sh stop            # Stop all services
./dev.sh status          # Check status
./dev.sh clean           # Clean restart
./dev.sh docker-start    # Start Docker services
./dev.sh logs kafka      # View logs

// ❌ WRONG: Don't use direct commands
docker-compose up
mvn spring-boot:run
java -jar service.jar
`,

  creatingNewScript: `
// When you need a new operation:

// 1. Create script in scripts/ folder
// scripts/my-new-operation.sh
#!/bin/bash
echo "Performing my operation..."
# Your logic here

// 2. Make it executable
chmod +x scripts/my-new-operation.sh

// 3. Add to dev.sh case statement
"my-operation")
    echo "🚀 Running my operation..."
    exec "$SCRIPTS_DIR/my-new-operation.sh" "\${@:2}"
    ;;

// 4. Update help section in dev.sh
echo "  my-operation             Description of operation"
`,
};

const ARCHITECTURE_DECISIONS = {
  "Why file: protocol instead of npm link": {
    reason: "npm link creates symlinks that cause issues with Turbopack and module resolution",
    solution: "Use file: protocol with transpilePackages in next.config.ts",
    status: "Active",
  },

  "Why UPPERCASE enums": {
    reason: "PostgreSQL enum values are UPPERCASE, must match exactly to prevent runtime failures",
    solution: "All TypeScript enum schemas use UPPERCASE values",
    status: "Active",
  },

  "Why shared GraphQL client": {
    reason: "Avoid code duplication and maintain single source of truth for API operations",
    solution: "Single @modern-reservation/graphql-client package with all queries/mutations",
    status: "Active",
  },

  "Why no Turbopack": {
    reason: "Turbopack has issues resolving file: protocol dependencies",
    solution: "Use standard Webpack bundler in Next.js",
    status: "Active",
  },
};

// Create MCP server
const server = new Server(
  {
    name: "modern-reservation-rules",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
      prompts: {},
    },
  }
);

// Register tools
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_project_rules",
        description: "Get project-specific rules and guidelines for Modern Reservation project",
        inputSchema: {
          type: "object",
          properties: {
            category: {
              type: "string",
              description: "Category of rules to retrieve (general, schemas, graphql, architecture, database, dependencies, documentation, buildTools, or 'all')",
              enum: ["general", "schemas", "graphql", "architecture", "database", "dependencies", "documentation", "buildTools", "all"],
            },
          },
          required: [],
        },
      },
      {
        name: "get_code_patterns",
        description: "Get correct code patterns and anti-patterns for Modern Reservation project",
        inputSchema: {
          type: "object",
          properties: {
            pattern: {
              type: "string",
              description: "Pattern to retrieve (importingSchemas, importingGraphQL, monorepoPackage, enumValues, or 'all')",
              enum: ["importingSchemas", "importingGraphQL", "monorepoPackage", "enumValues", "all"],
            },
          },
          required: [],
        },
      },
      {
        name: "get_architecture_decisions",
        description: "Get architecture decisions and their reasoning",
        inputSchema: {
          type: "object",
          properties: {
            decision: {
              type: "string",
              description: "Specific decision to retrieve, or 'all' for all decisions",
            },
          },
          required: [],
        },
      },
      {
        name: "validate_approach",
        description: "Validate if a proposed approach follows project guidelines",
        inputSchema: {
          type: "object",
          properties: {
            description: {
              type: "string",
              description: "Description of the approach to validate",
            },
          },
          required: ["description"],
        },
      },
    ],
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  switch (name) {
    case "get_project_rules": {
      const category = args?.category || "all";

      if (category === "all") {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(PROJECT_RULES, null, 2),
            },
          ],
        };
      }

      if (PROJECT_RULES[category as keyof typeof PROJECT_RULES]) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                category,
                rules: PROJECT_RULES[category as keyof typeof PROJECT_RULES],
              }, null, 2),
            },
          ],
        };
      }

      throw new Error(`Unknown category: ${category}`);
    }

    case "get_code_patterns": {
      const pattern = args?.pattern || "all";

      if (pattern === "all") {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(COMMON_PATTERNS, null, 2),
            },
          ],
        };
      }

      if (COMMON_PATTERNS[pattern as keyof typeof COMMON_PATTERNS]) {
        return {
          content: [
            {
              type: "text",
              text: COMMON_PATTERNS[pattern as keyof typeof COMMON_PATTERNS],
            },
          ],
        };
      }

      throw new Error(`Unknown pattern: ${pattern}`);
    }

    case "get_architecture_decisions": {
      const decision = args?.decision || "all";

      if (decision === "all") {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(ARCHITECTURE_DECISIONS, null, 2),
            },
          ],
        };
      }

      if (ARCHITECTURE_DECISIONS[decision as keyof typeof ARCHITECTURE_DECISIONS]) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify({
                decision,
                details: ARCHITECTURE_DECISIONS[decision as keyof typeof ARCHITECTURE_DECISIONS],
              }, null, 2),
            },
          ],
        };
      }

      throw new Error(`Unknown decision: ${decision}`);
    }

    case "validate_approach": {
      const description = (args?.description as string) || "";
      const violations: string[] = [];
      const recommendations: string[] = [];

      // Check for common anti-patterns
      if (description.toLowerCase().includes("create readme") ||
          description.toLowerCase().includes("new readme")) {
        violations.push("❌ Do not create unnecessary README files");
        recommendations.push("✅ Update existing README.md or add to docs/ directory");
      }

      if (description.toLowerCase().includes("npm link")) {
        violations.push("❌ Do not use npm link for monorepo dependencies");
        recommendations.push("✅ Use file: protocol in package.json");
      }

      if (description.includes("lowercase") && description.includes("enum")) {
        violations.push("❌ Enum values must be UPPERCASE to match PostgreSQL");
        recommendations.push("✅ Use UPPERCASE enum values: 'PENDING', 'CONFIRMED', etc.");
      }

      if (description.toLowerCase().includes("apollo client") &&
          !description.includes("shared")) {
        violations.push("❌ Do not create separate Apollo clients");
        recommendations.push("✅ Use @modern-reservation/graphql-client");
      }

      if (description.toLowerCase().includes("schema") &&
          description.toLowerCase().includes("create") &&
          !description.includes("shared")) {
        violations.push("❌ Do not create local schema definitions");
        recommendations.push("✅ Use schemas from @modern-reservation/schemas");
      }

      // Check for missing system commands/packages
      if (description.toLowerCase().includes("command") &&
          description.toLowerCase().includes("not found")) {
        const commandMatch = description.match(/command[:\s]+['"]?(\w+)['"]?/i);
        if (commandMatch) {
          const command = commandMatch[1];
          recommendations.push(`✅ Install missing package: sudo apt install ${command}`);
          recommendations.push(`💡 Common commands: tree, curl, wget, jq, make, build-essential`);
        }
      }

      // Check for direct docker/java commands instead of dev.sh
      if ((description.toLowerCase().includes("docker-compose") ||
           description.toLowerCase().includes("docker compose") ||
           description.toLowerCase().includes("mvn spring-boot:run") ||
           description.toLowerCase().includes("java -jar")) &&
          !description.toLowerCase().includes("dev.sh")) {
        violations.push("❌ Do not use direct docker/java commands");
        recommendations.push("✅ Use ./dev.sh script instead");
        recommendations.push("💡 Available: ./dev.sh start, stop, status, clean, docker-start, logs");
      }

      // Check if they need a new script
      if ((description.toLowerCase().includes("new script") ||
           description.toLowerCase().includes("create script")) &&
          !description.includes("scripts/")) {
        recommendations.push("✅ Create script in scripts/ folder (e.g., scripts/my-operation.sh)");
        recommendations.push("✅ Make it executable: chmod +x scripts/my-operation.sh");
        recommendations.push("✅ Add to dev.sh case statement to integrate it");
        recommendations.push("💡 Use kebab-case naming (e.g., check-health.sh)");
      }

      // Suggest using dev.sh for start/stop operations
      if ((description.toLowerCase().includes("start") ||
           description.toLowerCase().includes("stop") ||
           description.toLowerCase().includes("restart")) &&
          description.toLowerCase().includes("service") &&
          !description.includes("dev.sh")) {
        recommendations.push("✅ Use ./dev.sh for service management");
        recommendations.push("💡 ./dev.sh start - Start all services");
        recommendations.push("💡 ./dev.sh stop - Stop all services");
        recommendations.push("💡 ./dev.sh clean - Clean restart");
      }

      const isValid = violations.length === 0;

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify({
              valid: isValid,
              violations,
              recommendations,
              message: isValid
                ? "✅ Approach follows project guidelines"
                : "⚠️  Approach violates project guidelines",
            }, null, 2),
          },
        ],
      };
    }

    default:
      throw new Error(`Unknown tool: ${name}`);
  }
});

// Register prompts
server.setRequestHandler(ListPromptsRequestSchema, async () => {
  return {
    prompts: [
      {
        name: "project_guidelines",
        description: "Get a comprehensive overview of Modern Reservation project guidelines",
      },
      {
        name: "schema_best_practices",
        description: "Get best practices for working with schemas in this project",
      },
      {
        name: "monorepo_setup",
        description: "Get guidance on monorepo setup and dependency management",
      },
    ],
  };
});

server.setRequestHandler(GetPromptRequestSchema, async (request) => {
  const { name } = request.params;

  switch (name) {
    case "project_guidelines":
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `# Modern Reservation Project Guidelines

## Key Rules:
${Object.entries(PROJECT_RULES).map(([category, rules]) => `
### ${category.charAt(0).toUpperCase() + category.slice(1)}
${rules.map((rule: string) => `- ${rule}`).join('\n')}
`).join('\n')}

## Architecture Decisions:
${Object.entries(ARCHITECTURE_DECISIONS).map(([decision, details]) => `
### ${decision}
- **Reason**: ${details.reason}
- **Solution**: ${details.solution}
- **Status**: ${details.status}
`).join('\n')}`,
            },
          },
        ],
      };

    case "schema_best_practices":
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `# Schema Best Practices

## Always Use Shared Schemas
- Import from @modern-reservation/schemas
- DO NOT create duplicate schema definitions
- All schemas are in libs/shared/schemas

## Enum Guidelines
- All enum values MUST be UPPERCASE
- Match PostgreSQL enum values exactly
- Example: 'PENDING', 'CONFIRMED', 'CHECKED_IN'

## Code Example:
${COMMON_PATTERNS.importingSchemas}

${COMMON_PATTERNS.enumValues}`,
            },
          },
        ],
      };

    case "monorepo_setup":
      return {
        messages: [
          {
            role: "user",
            content: {
              type: "text",
              text: `# Monorepo Setup Guide

## Dependency Management
- Use file: protocol for internal dependencies
- DO NOT use npm link or workspace:*
- Add transpilePackages in next.config.ts

## Example:
${COMMON_PATTERNS.monorepoPackage}

## Architecture Decision:
${JSON.stringify(ARCHITECTURE_DECISIONS["Why file: protocol instead of npm link"], null, 2)}`,
            },
          },
        ],
      };

    default:
      throw new Error(`Unknown prompt: ${name}`);
  }
});

// Start server
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Modern Reservation MCP Server running on stdio");
}

main().catch((error) => {
  console.error("Server error:", error);
  process.exit(1);
});
