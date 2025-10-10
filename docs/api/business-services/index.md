# Business Services API Documentation

Welcome to the Modern Reservation System Business Services API documentation. This section covers all the core business microservices that power our reservation platform.

## 🏗️ Architecture Overview

Our business services are built using **Java Spring Boot** with **OpenTelemetry** observability and **event-driven architecture** via Apache Kafka.

## 📋 Available Services

| Service | Purpose | Port | Status | Documentation |
|---------|---------|------|--------|---------------|
| [**Analytics Engine**](./analytics-engine/) | Real-time analytics and reporting | 8080 | ✅ Active | [API Docs](./analytics-engine/) |
| [**Availability Calculator**](./availability-calculator/) | Room availability computation | 8081 | ✅ Active | [API Docs](./availability-calculator/) |
| [**Payment Processor**](./payment-processor/) | Secure payment handling | 8082 | ✅ Active | [API Docs](./payment-processor/) |
| [**Rate Management**](./rate-management/) | Dynamic pricing engine | 8083 | ✅ Active | [API Docs](./rate-management/) |
| [**Reservation Engine**](./reservation-engine/) | Core booking logic | 8084 | ✅ Active | [API Docs](./reservation-engine/) |
| [**Batch Processor**](./batch-processor/) | Background job processing | 8085 | 🚧 Development | [API Docs](./batch-processor/) |

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+
- Apache Kafka 3.x

### Quick Start
```bash
# Start all infrastructure services
./dev.sh docker-start

# Build all business services
mvn clean install -f apps/backend/java-services/

# Start business services
./dev.sh start-business-services
```

## 🔍 Service Discovery

All business services are registered with **Eureka Server** for service discovery:
- **Eureka Dashboard**: http://localhost:8761
- **Service Health**: All services expose `/actuator/health` endpoints

## 📊 Observability

### OpenTelemetry Integration
All services include distributed tracing:
- **Jaeger UI**: http://localhost:16686
- **Prometheus Metrics**: http://localhost:9090
- **Grafana Dashboards**: http://localhost:3000

### Health Monitoring
```bash
# Check all service health
./dev.sh status

# View service logs
./dev.sh logs <service-name>
```

## 🔐 Security

All business services implement:
- **JWT Authentication** via API Gateway
- **Multi-tenant data isolation**
- **Rate limiting** (1000 req/min per tenant)
- **CORS configuration**
- **SQL injection prevention**

## 📡 Event-Driven Communication

Services communicate via **Apache Kafka** topics:

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `reservation.created` | Reservation Engine | Analytics, Payment | New reservation events |
| `payment.processed` | Payment Processor | Reservation, Analytics | Payment confirmation |
| `availability.updated` | Availability Calculator | Rate Management | Room availability changes |
| `rate.adjusted` | Rate Management | Reservation Engine | Dynamic pricing updates |

## 📈 Performance Specifications

| Metric | Target | Current |
|--------|--------|---------|
| **Response Time** | <50ms (95th percentile) | ✅ 35ms |
| **Throughput** | 10,000 req/min | ✅ 12,000 req/min |
| **Availability** | 99.9% uptime | ✅ 99.95% |
| **Error Rate** | <0.1% | ✅ 0.05% |

## 🛠️ Development

### Local Development
```bash
# Run single service in development mode
cd apps/backend/java-services/business-services/<service-name>
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debugging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -P integration-tests

# Generate test coverage report
mvn jacoco:report
```

## 📚 Additional Resources

- [**Architecture Documentation**](../../architecture/)
- [**Deployment Guide**](../../deployment/)
- [**OpenTelemetry Configuration**](../../references/AVRO_QUICK_REFERENCE.md)
- [**Multi-Tenancy Implementation**](../../MULTI_TENANCY.md)

## 🐛 Troubleshooting

### Common Issues

**Service Not Starting**
```bash
# Check dependencies
./dev.sh docker-status

# Check service logs
docker logs modern-reservation-<service-name>

# Restart specific service
./dev.sh restart <service-name>
```

**Database Connection Issues**
```bash
# Verify database connectivity
./dev.sh check-db

# Reset database schema
./dev.sh setup-database
```

**Kafka Connection Issues**
```bash
# Check Kafka cluster health
./dev.sh kafka-status

# View Kafka topics
docker exec -it modern-reservation-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

For specific service documentation, please navigate to the individual service pages using the links above.
