# Shared Libraries

## Overview
Cross-service shared libraries to ensure consistency, reduce code duplication, and maintain type safety across the entire hybrid architecture.

## Libraries

### 1. Schemas (`schemas/`)
- **Purpose**: Unified data validation and type definitions
- **Tech**: Zod schemas for TypeScript-first validation
- **Features**:
  - Reservation schemas
  - Guest profile schemas
  - Payment schemas
  - Room and amenity schemas
  - API request/response schemas
  - Cross-service validation
- **Usage**: Shared between Node.js and Java services via code generation

### 2. UI Components (`ui-components/`)
- **Purpose**: Reusable Angular Material components
- **Tech**: Angular 17+ with Material Design
- **Features**:
  - Reservation forms and wizards
  - Data tables and grids
  - Charts and analytics widgets
  - Navigation components
  - Common dialogs and modals
  - Theme-aware components
- **Usage**: Imported by all frontend applications

### 3. Backend Utils (`backend-utils/`)
- **Purpose**: Common backend utilities and middleware
- **Tech**: Node.js/TypeScript utilities
- **Features**:
  - Database connection utilities
  - Redis cache helpers
  - Kafka producer/consumer utilities
  - Authentication middleware
  - Logging utilities
  - Error handling middleware
  - Rate limiting utilities
- **Usage**: Shared across Node.js microservices

### 4. Testing Utils (`testing-utils/`)
- **Purpose**: Common testing utilities and mocks
- **Tech**: Jest, Cypress, Test containers
- **Features**:
  - Mock data generators
  - Database test fixtures
  - API testing utilities
  - End-to-end test helpers
  - Performance testing utilities
  - Integration test setup
- **Usage**: All services and applications

### 5. GraphQL Schemas (`graphql-schemas/`)
- **Purpose**: Federated GraphQL schema definitions
- **Tech**: GraphQL Federation with Apollo
- **Features**:
  - Reservation schema definitions
  - User and authentication schemas
  - Property and room schemas
  - Payment and billing schemas
  - Analytics and reporting schemas
  - Schema federation directives
- **Usage**: Code generation for frontend and Node.js services

### 6. Constants (`constants/`)
- **Purpose**: System-wide constants and enumerations
- **Tech**: TypeScript constants and enums
- **Features**:
  - Room status enums
  - Reservation status constants
  - Payment status enums
  - User role definitions
  - API endpoints
  - Configuration constants
- **Usage**: All services and applications

## Library Structure Template
```
lib-name/
├── src/
│   ├── index.ts          # Main export file
│   ├── types/           # Type definitions
│   ├── utils/           # Utility functions
│   └── constants/       # Constants
├── tests/               # Unit tests
├── package.json         # Dependencies
├── tsconfig.json        # TypeScript config
├── jest.config.js       # Test configuration
└── README.md           # Documentation
```

## Code Generation Pipeline
- **Schema-First Development**: Zod schemas generate TypeScript types
- **GraphQL Code Generation**: Generates typed client code
- **OpenAPI Generation**: REST API types from schema
- **Cross-Language Support**: Protocol Buffers for Java/Node.js interop

## Version Management
- **Semantic Versioning**: Major.Minor.Patch versioning
- **Backward Compatibility**: Non-breaking changes within major versions
- **Migration Guides**: Documentation for breaking changes
- **Automated Testing**: Version compatibility testing
