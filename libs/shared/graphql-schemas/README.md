# GraphQL Schemas

This library contains the modular GraphQL schemas for the Modern Reservation System using Apollo Federation.

## 🏗️ Architecture

The schemas are organized as **modular, domain-focused files** to improve development workflow:

- **Individual Domain Files**: Each business domain has its own focused GraphQL file
- **Master Composition**: Centralized orchestration via `master-schema.graphql`
- **Federation-Ready**: Apollo Federation directives for microservices architecture

## 📁 File Structure

```
graphql-schemas/
├── types/                      # Modular schema files
│   ├── common.graphql         # Base types, interfaces, unions
│   ├── property.graphql       # Property management types
│   ├── guest.graphql          # Guest management types
│   ├── user.graphql           # User management & auth types
│   ├── reservation.graphql    # Reservation core types
│   ├── payment.graphql        # Payment & billing types
│   ├── availability.graphql   # Availability & rate types
│   ├── analytics.graphql      # Analytics & reporting types
│   ├── housekeeping.graphql   # Housekeeping & maintenance
│   ├── channel.graphql        # Channel management types
│   └── review.graphql         # Review & rating types
├── master-schema.graphql      # Federation composition config
├── compose-schema.sh          # Schema composition script
└── README.md                  # This file
```

## 🚀 Usage

### Development Workflow

1. **Edit Individual Files**: Modify specific domain files in `types/` directory
2. **Automatic Composition**: Use the composition script to combine schemas
3. **Federation Gateway**: Deploy composed schema to Apollo Gateway

### Schema Composition

```bash
# Make script executable (first time only)
chmod +x compose-schema.sh

# Compose all modular schemas
./compose-schema.sh
```

This creates:
- `composed-schema.graphql` - Single file with all schemas combined
- Validates schema correctness (if GraphQL CLI available)
- Generates TypeScript types (if CodeGen available)

### Service Integration

```typescript
// In your GraphQL service
import { buildFederatedSchema } from '@apollo/federation';
import { readFileSync } from 'fs';
import { resolve } from 'path';

const typeDefs = readFileSync(
  resolve(__dirname, '../graphql-schemas/types/property.graphql'),
  'utf8'
);

const schema = buildFederatedSchema({ typeDefs, resolvers });
```

### Gateway Configuration

```typescript
// Apollo Gateway setup
import { ApolloGateway } from '@apollo/gateway';

const gateway = new ApolloGateway({
  serviceList: [
    { name: 'property-service', url: 'http://localhost:4001/graphql' },
    { name: 'guest-service', url: 'http://localhost:4002/graphql' },
    { name: 'reservation-service', url: 'http://localhost:4003/graphql' },
    // ... other services
  ],
});
```

## 🔧 Development Benefits

### Why Modular?
- **Focused Files**: Each domain is isolated and easy to understand
- **Reduced Confusion**: No more searching through massive monolithic files
- **Faster Development**: Quick location of relevant types and fields
- **Better Collaboration**: Multiple developers can work on different domains
- **Easier Debugging**: Issues are contained within specific domain contexts

### Schema Organization Principles
- **Domain-Driven**: Files organized by business domain boundaries
- **Dependency Order**: Common types first, specialized types build upon them
- **Federation Directives**: Proper `@key`, `@external`, `@provides`, `@requires` usage
- **Type Safety**: Consistent TypeScript generation across all services

## 🛠️ Tools & Scripts

### compose-schema.sh
Combines all modular schema files into a single composed schema:
- Loads files in correct dependency order
- Validates schema correctness
- Generates TypeScript types
- Creates deployment-ready schema file

### Recommended Dev Tools
```bash
# Install GraphQL CLI for validation
npm install -g @graphql-cli/cli

# Install CodeGen for TypeScript types
npm install -g @graphql-codegen/cli

# Create codegen.yml for type generation
npx graphql-codegen init
```

## 🔍 Federation Patterns

### Entity Definitions
```graphql
# In property.graphql
type Property @key(fields: "id") {
  id: ID!
  name: String!
  address: Address!
}

# In reservation.graphql
extend type Property @key(fields: "id") {
  id: ID! @external
  reservations: [Reservation!]!
}
```

### Cross-Service References
```graphql
# Property service owns Property
type Property @key(fields: "id") {
  id: ID!
  name: String!
}

# Reservation service extends Property
extend type Property @key(fields: "id") {
  id: ID! @external
  reservations: [Reservation!]! @provides(fields: "status")
}
```

## 📚 Additional Resources

- [Apollo Federation Documentation](https://www.apollographql.com/docs/federation/)
- [GraphQL Schema Design Best Practices](https://www.apollographql.com/blog/graphql-schema-design-best-practices/)
- [Modular GraphQL Schemas](https://www.apollographql.com/blog/modularizing-your-graphql-schema-code/)

## Federation Setup

### Gateway Configuration
```typescript
import { ApolloGateway } from '@apollo/gateway';
import { ApolloServer } from 'apollo-server-express';

const gateway = new ApolloGateway({
  serviceList: [
    { name: 'property-service', url: 'http://property-service:4001/graphql' },
    { name: 'guest-service', url: 'http://guest-service:4002/graphql' },
    { name: 'reservation-service', url: 'http://reservation-service:4003/graphql' },
    { name: 'payment-service', url: 'http://payment-service:4004/graphql' },
    { name: 'rate-service', url: 'http://rate-service:4005/graphql' },
    { name: 'availability-service', url: 'http://availability-service:4006/graphql' },
    { name: 'housekeeping-service', url: 'http://housekeeping-service:4007/graphql' },
    { name: 'analytics-service', url: 'http://analytics-service:4008/graphql' },
    { name: 'channel-service', url: 'http://channel-service:4009/graphql' },
    { name: 'user-service', url: 'http://user-service:4010/graphql' },
  ],
  introspectionHeaders: {
    'x-api-key': process.env.INTERNAL_API_KEY,
  },
});
```

### Service Configuration
Each microservice needs to implement its portion of the federated schema:

```typescript
// Example for Property Service
import { buildFederatedSchema } from '@apollo/federation';

const typeDefs = gql`
  extend type Query {
    property(id: UUID!): Property
    properties(filter: PropertyFilterInput): [Property!]!
  }

  type Property @key(fields: "id") {
    id: UUID!
    name: String!
    # ... other fields
  }
`;

const resolvers = {
  Property: {
    __resolveReference(object: { id: string }) {
      return getPropertyById(object.id);
    },
  },
  Query: {
    property: (_, { id }) => getPropertyById(id),
    properties: (_, { filter }) => getProperties(filter),
  },
};

const schema = buildFederatedSchema([{ typeDefs, resolvers }]);
```

## Key Features

### 1. Entity Federation
- Properties, Rooms, Guests, and Reservations are federated entities
- Cross-service relationships through `@key` directives
- Reference resolvers for entity expansion

### 2. Real-time Subscriptions
- WebSocket connections for live updates
- Property-specific and user-specific subscriptions
- Event-driven updates from Kafka streams

### 3. Caching Strategy
- Redis-based caching at gateway level
- Per-service caching for expensive operations
- Smart cache invalidation on mutations

### 4. Security
- Field-level permissions based on user roles
- Tenant isolation enforced at resolver level
- Rate limiting per client/operation

### 5. Performance Optimizations
- DataLoader for N+1 query prevention
- Query complexity analysis and limits
- Automatic persisted queries (APQ)

## Schema Patterns

### Entity Relationships
```graphql
type Property @key(fields: "id") {
  id: UUID!
  rooms: [Room!]! @requires(fields: "id")
}

type Room @key(fields: "id") {
  id: UUID!
  property: Property! @requires(fields: "propertyId")
}
```

### Computed Fields
```graphql
type Guest {
  id: UUID!
  firstName: String!
  lastName: String!
  fullName: String! # Computed from firstName + lastName
  lifetimeValue: Float! # Computed from payment history
}
```

### Input Validation
```graphql
input CreateReservationInput {
  propertyId: UUID! @constraint(format: "uuid")
  checkInDate: DateTime! @constraint(format: "date-time")
  checkOutDate: DateTime! @constraint(format: "date-time")
  adults: Int! @constraint(min: 1, max: 10)
}
```

## Testing

### Schema Validation
```bash
npm run validate-schema
```

### Federation Composition
```bash
rover supergraph compose --config ./supergraph.yaml
```

### Integration Tests
```bash
npm run test:integration
```

## Deployment

### Docker Configuration
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 4000
CMD ["npm", "start"]
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: graphql-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: graphql-gateway
  template:
    metadata:
      labels:
        app: graphql-gateway
    spec:
      containers:
      - name: gateway
        image: modern-reservation/graphql-gateway:latest
        ports:
        - containerPort: 4000
        env:
        - name: GATEWAY_PORT
          value: "4000"
        - name: REDIS_URL
          valueFrom:
            secretKeyRef:
              name: redis-secret
              key: url
```

## Monitoring

### Metrics
- Query execution time
- Federation composition time
- Cache hit/miss ratios
- Error rates by service

### Tracing
- Distributed tracing with Jaeger
- Apollo Studio integration
- Performance monitoring

### Health Checks
```graphql
type Query {
  _service: _Service!
  _health: HealthCheck!
}

type HealthCheck {
  status: String!
  services: [ServiceHealth!]!
}
```

## Best Practices

1. **Schema Design**
   - Use meaningful names and descriptions
   - Prefer flat structures over deep nesting
   - Include proper nullability annotations

2. **Performance**
   - Implement DataLoaders for all N+1 scenarios
   - Use connection patterns for pagination
   - Cache expensive computed fields

3. **Security**
   - Validate all inputs at schema level
   - Implement proper authorization
   - Use allowlists for production queries

4. **Monitoring**
   - Log all resolver execution times
   - Monitor federation gateway health
   - Track schema usage patterns
