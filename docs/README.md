# Modern Reservation System Documentation

Welcome to the Modern Reservation System documentation site! This is a comprehensive cloud-native hotel reservation platform built with microservices and event-driven architecture.

## Quick Navigation

### 🚀 Getting Started
- [Quick Start Guide](guides/DEV_QUICK_REFERENCE.md)
- [Clean Restart Guide](guides/CLEAN_RESTART_GUIDE.md)
- [Script Organization](guides/SCRIPT_ORGANIZATION.md)
- [Single Entry Point](guides/SINGLE_ENTRY_POINT.md)

### 🏗️ Architecture
- [Event-Driven Architecture](architecture/event-driven-architecture-diagram.md)
- [Kafka Implementation](architecture/KAFKA_IMPLEMENTATION_GUIDE.md)
- [Kafka Quick Start](architecture/KAFKA_QUICK_START.md)
- [Implementation Plan](architecture/IMPLEMENTATION_PLAN.md)
- [Kafka Summary](architecture/KAFKA_SUMMARY.md)

### 📊 References
- [Avro Quick Reference](references/AVRO_QUICK_REFERENCE.md)
- [Avro Migration Guide](references/AVRO_MIGRATION_COMPLETE.md)

### 🚀 Deployment
- [Network Isolation Guide](deployment/network-isolation-guide.md)

### 📋 Project Info
- [Product Requirements](product-requirements-document.md)
- [Development Plan](project-development-plan.md)
- [Phase 0 Complete](PHASE_0_COMPLETE.md)

## Project Structure

```
docs/
├── index.md                          # GitHub Pages landing
├── README.md                         # This file
├── guides/                           # Development guides
│   ├── DEV_QUICK_REFERENCE.md
│   ├── CLEAN_RESTART_GUIDE.md
│   ├── SCRIPT_ORGANIZATION.md
│   └── SINGLE_ENTRY_POINT.md
├── architecture/                     # Architecture docs
│   ├── event-driven-architecture-diagram.md
│   ├── KAFKA_IMPLEMENTATION_GUIDE.md
│   └── ...
├── references/                       # Technical references
│   ├── AVRO_QUICK_REFERENCE.md
│   └── AVRO_MIGRATION_COMPLETE.md
├── deployment/                       # Deployment guides
│   └── network-isolation-guide.md
└── [project management docs]
```

## Tech Stack

- **Backend**: Java (Spring Boot) + Node.js (NestJS)
- **Event Streaming**: Apache Kafka + Avro (Schema Registry)
- **Database**: PostgreSQL
- **Caching**: Redis
- **Service Discovery**: Eureka
- **Distributed Tracing**: Zipkin
- **Monitoring**: Kafka UI, PgAdmin
- **Container**: Docker

## Quick Commands

```bash
# Start everything
./dev.sh start

# Clean restart
./dev.sh clean

# Check status
./dev.sh status

# Test Avro events
./dev.sh test-avro
```

## Documentation Sections

### 1. Development Guides (`guides/`)
- **DEV Quick Reference**: Daily development commands
- **Clean Restart Guide**: Fresh environment setup
- **Script Organization**: Understanding dev scripts
- **Single Entry Point**: dev.sh command reference

### 2. Architecture Documentation (`architecture/`)
- **Event-Driven Architecture**: System architecture overview
- **Kafka Implementation**: Event streaming details
- **Implementation Plan**: Development roadmap

### 3. Technical References (`references/`)
- **Avro Quick Reference**: Working with Avro schemas
- **Avro Migration**: Migration guide and results

### 4. Deployment (`deployment/`)
- **Network Isolation**: Security and networking
- `monitoring-setup.md` - Observability stack deployment
- `disaster-recovery.md` - DR procedures and testing

### 4. Development Documentation (`development/`)
- **Getting Started**: Local development environment setup
- **Coding Standards**: Code style, conventions, and best practices
- **Testing Strategy**: Unit, integration, and E2E testing approaches
- **Database Migrations**: Schema change management
- **Debugging Guide**: Troubleshooting and debugging techniques
- **Performance Optimization**: Performance tuning guidelines

**Development Guides:**
- `getting-started.md` - Local development setup
- `coding-standards.md` - Code style and conventions
- `testing-strategy.md` - Comprehensive testing approach
- `database-migrations.md` - Schema versioning and migrations
- `debugging-guide.md` - Troubleshooting procedures
- `performance-optimization.md` - Performance tuning guide

### 5. Architectural Decision Records (`adr/`)
Chronological record of important architectural decisions with context, alternatives considered, and rationale.

**Sample ADRs:**
- `001-hybrid-nodejs-java-architecture.md` - Hybrid technology choice
- `002-event-driven-microservices.md` - Event-driven architecture adoption
- `003-graphql-federation-api.md` - GraphQL federation over REST
- `004-multi-master-database.md` - Database scaling strategy
- `005-redis-cluster-caching.md` - Caching architecture decisions
- `006-kafka-event-streaming.md` - Event streaming platform choice
- `007-kubernetes-orchestration.md` - Container orchestration platform
- `008-istio-service-mesh.md` - Service mesh adoption
- `009-observability-stack.md` - Monitoring and observability tools
- `010-security-compliance.md` - Security and compliance approach

## Documentation Standards

### Format and Style
- **Markdown**: All documentation in Markdown format
- **Diagrams**: Mermaid diagrams for architecture visualization
- **Code Examples**: Syntax-highlighted code blocks
- **Versioning**: Documentation versioned with code releases
- **Templates**: Consistent templates for different document types

### Content Guidelines
- **Clarity**: Clear, concise, and actionable content
- **Completeness**: Comprehensive coverage of topics
- **Currency**: Regular updates to maintain accuracy
- **Accessibility**: Documentation accessible to all team members
- **Searchability**: Well-structured with proper headings and tags

### Review Process
- **Peer Review**: All documentation changes reviewed
- **Technical Review**: Architecture decisions reviewed by senior engineers
- **Update Process**: Regular documentation maintenance schedule
- **Feedback Loop**: Continuous improvement based on user feedback

## Auto-Generated Documentation

### API Documentation
- **GraphQL**: Automatic schema documentation generation
- **REST**: OpenAPI specification auto-generation from code
- **Code Documentation**: JSDoc/JavaDoc integration
- **Changelog**: Automated changelog generation from commits

### Architecture Diagrams
- **System Diagrams**: Auto-generated from infrastructure code
- **Database Schema**: Generated from migration files
- **Dependency Graphs**: Service dependency visualization
- **Performance Metrics**: Real-time performance dashboards

## Documentation Maintenance

### Regular Updates
- **Weekly**: API documentation updates from schema changes
- **Monthly**: Architecture documentation review and updates
- **Quarterly**: Comprehensive documentation audit
- **Release**: Documentation updates with each release

### Quality Assurance
- **Link Checking**: Automated broken link detection
- **Spelling/Grammar**: Automated proofreading tools
- **Accuracy Verification**: Regular accuracy audits
- **User Testing**: Documentation usability testing

## Knowledge Management
- **Searchable**: Full-text search capabilities
- **Categorized**: Logical organization and categorization
- **Cross-Referenced**: Extensive cross-referencing between documents
- **Version History**: Complete change history tracking
- **Access Control**: Role-based access to sensitive documentation
