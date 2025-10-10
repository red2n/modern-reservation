---
layout: default
title: Modern Reservation System
---

# 🏨 Modern Reservation System Documentation

Welcome to the comprehensive documentation for the Modern Reservation System - a cloud-native, microservices-based hotel reservation platform built with modern technologies and event-driven architecture.

## 🚀 Quick Start

- **[Getting Started](../README.md)** - Setup and run the application
- **[DEV Quick Reference](guides/DEV_QUICK_REFERENCE.md)** - Daily development commands
- **[Clean Restart Guide](guides/CLEAN_RESTART_GUIDE.md)** - Fresh environment setup

## 📚 Documentation Sections

| Section | Description | Link |
|---------|-------------|------|
| **🔌 API Documentation** | Complete API reference for all services | [View API Docs](./api/) |
| **🏢 Business Services** | Java Spring Boot microservices documentation | [View Business Services](./api/business-services/) |
| **🏗️ Architecture & Design** | System design and architectural decisions | [View Architecture](./architecture/) |
| **🚀 Deployment** | Infrastructure and deployment guides | [View Deployment](./deployment/) |
| **📖 Developer Guides** | Development workflows and best practices | [View Guides](./guides/) |

### 🔌 **API Documentation**
- **[Business Services API](api/business-services/)** - Java Spring Boot microservices
- **[Analytics Engine](api/business-services/analytics-engine/)** - Real-time analytics and reporting
- **[Availability Calculator](api/business-services/availability-calculator/)** - Room availability computation
- **[Payment Processor](api/business-services/payment-processor/)** - Secure payment handling
- **[Rate Management](api/business-services/rate-management/)** - Dynamic pricing engine
- **[Reservation Engine](api/business-services/reservation-engine/)** - Core booking logic

### 🏗️ Architecture & Design
- **[Event-Driven Architecture](architecture/event-driven-architecture-diagram.md)** - System architecture overview
- **[Kafka Implementation Guide](architecture/KAFKA_IMPLEMENTATION_GUIDE.md)** - Event streaming implementation
- **[Kafka Quick Start](architecture/KAFKA_QUICK_START.md)** - Getting started with Kafka
- **[Implementation Plan](architecture/IMPLEMENTATION_PLAN.md)** - Development roadmap
- **[Kafka Summary](architecture/KAFKA_SUMMARY.md)** - Kafka integration overview

### 🔧 Development Guides
- **[DEV Quick Reference](guides/DEV_QUICK_REFERENCE.md)** - Daily development workflow
- **[Script Organization](guides/SCRIPT_ORGANIZATION.md)** - Development scripts overview
- **[Single Entry Point](guides/SINGLE_ENTRY_POINT.md)** - Unified dev.sh command reference
- **[Clean Restart Guide](guides/CLEAN_RESTART_GUIDE.md)** - Environment reset procedures

### 📊 Event Streaming (Avro & Kafka)
- **[Avro Quick Reference](references/AVRO_QUICK_REFERENCE.md)** - Working with Avro schemas
- **[Avro Migration Complete](references/AVRO_MIGRATION_COMPLETE.md)** - Migration guide and results
- **[Kafka Quick Start](architecture/KAFKA_QUICK_START.md)** - Kafka development guide
- **[Kafka Implementation](architecture/KAFKA_IMPLEMENTATION_GUIDE.md)** - Detailed implementation

### 🚀 Deployment
- **[Network Isolation Guide](deployment/network-isolation-guide.md)** - Security and networking

### 📋 Project Management
- **[Product Requirements Document](product-requirements-document.md)** - Business requirements
- **[Project Development Plan](project-development-plan.md)** - Development timeline
- **[Phase 0 Complete](PHASE_0_COMPLETE.md)** - Initial setup completion

## 🎯 Common Tasks

### Daily Development
```bash
# Start the system
./dev.sh start

# Check status
./dev.sh status

# View logs
./dev.sh logs reservation-engine

# Stop services
./dev.sh stop
```

### Fresh Environment
```bash
# Complete clean restart
./dev.sh clean

# With options
./dev.sh clean --keep-data
./dev.sh clean --skip-maven
```

### Testing
```bash
# Test Avro events
./dev.sh test-avro

# Check dependencies
./dev.sh check-deps

# Health check
./dev.sh check-health
```

## 🏗️ System Architecture

### Technology Stack
- **Backend Services**: Java (Spring Boot) + Node.js (NestJS)
- **Event Streaming**: Apache Kafka with Avro (Schema Registry)
- **Database**: PostgreSQL
- **Caching**: Redis
- **API Gateway**: Node.js (NestJS)
- **Service Discovery**: Eureka
- **Distributed Tracing**: Zipkin
- **Containerization**: Docker

### Microservices
- **Java Services**:
  - Reservation Engine
  - Payment Processor
  - Rate Management
  - Availability Calculator
  - Analytics Engine
  - Batch Processor

- **Node Services**:
  - API Gateway
  - Notification Service
  - Audit Service
  - Channel Manager
  - WebSocket Service
  - Housekeeping Service
  - File Upload Service

### Infrastructure Services
- **Kafka** (Port 9092) - Event streaming
- **Schema Registry** (Port 8085) - Avro schema management
- **PostgreSQL** (Port 5432) - Primary database
- **Redis** (Port 6379) - Caching layer
- **Eureka** (Port 8761) - Service discovery
- **Zipkin** (Port 9411) - Distributed tracing
- **Kafka UI** (Port 8090) - Kafka monitoring
- **PgAdmin** (Port 5050) - Database administration

## 📖 Key Features

### Event-Driven Architecture
- **Asynchronous Processing**: Non-blocking event handling
- **Loose Coupling**: Services communicate via events
- **Scalability**: Horizontal scaling capabilities
- **Resilience**: Fault-tolerant event processing

### Avro Schema Evolution
- **Schema Registry**: Centralized schema management
- **Version Control**: Schema versioning and compatibility
- **Type Safety**: Strong typing for events
- **Backward Compatibility**: Safe schema evolution

### Development Experience
- **Single Entry Point**: `./dev.sh` for all operations
- **Clean Restart**: Fresh environment in minutes
- **Comprehensive Logging**: Full observability
- **Hot Reload**: Fast development iteration

## 🔗 Quick Links

### Web Interfaces
- [Kafka UI](http://localhost:8090) - Monitor Kafka topics and consumers
- [Eureka Dashboard](http://localhost:8761) - Service registry
- [Zipkin](http://localhost:9411) - Distributed tracing
- [PgAdmin](http://localhost:5050) - Database management
- [Schema Registry](http://localhost:8085) - Avro schemas

### Development Tools
- [Dev Quick Reference](guides/DEV_QUICK_REFERENCE.md) - Command cheat sheet
- [Clean Restart Guide](guides/CLEAN_RESTART_GUIDE.md) - Reset environment
- [Avro Quick Reference](references/AVRO_QUICK_REFERENCE.md) - Event development

## 🤝 Contributing

### Development Workflow
1. Review the [Project Development Plan](project-development-plan.md)
2. Check the [Implementation Plan](architecture/IMPLEMENTATION_PLAN.md)
3. Follow the [DEV Quick Reference](DEV_QUICK_REFERENCE.md)
4. Use `./dev.sh` for all operations

### Code Organization
- `/apps/backend/java-services/` - Java microservices
- `/apps/backend/node-services/` - Node.js services
- `/apps/frontend/` - Frontend applications
- `/libs/shared/` - Shared libraries
- `/infrastructure/` - Infrastructure as code
- `/scripts/` - Development scripts

## 📞 Support

For issues, questions, or contributions:
- Review the documentation in this folder
- Check the main [README](../README.md)
- Explore the [architecture guides](architecture/)

## 📄 License

[Your License Here]

---

**Last Updated**: October 7, 2025
**Version**: 1.0.0
**Repository**: [github.com/red2n/modern-reservation](https://github.com/red2n/modern-reservation)
