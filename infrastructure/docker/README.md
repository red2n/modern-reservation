# Modern Reservation System - Docker Infrastructure

This directory contains Docker configurations for running the Modern Reservation System infrastructure and services.

## Files Structure

```
infrastructure/docker/
├── docker-compose-infrastructure.yml  # External services (Zipkin, PostgreSQL, Redis)
├── docker-compose-services.yml       # Your application services
├── docker-compose.override.yml       # Development overrides
└── README.md                         # This file
```

## Quick Start

### 1. Start Infrastructure Services
```bash
cd infrastructure/docker
docker-compose -f docker-compose-infrastructure.yml up -d
```

This starts:
- **Zipkin** (Port 9411) - Distributed tracing
- **PostgreSQL** (Port 5432) - Database
- **Redis** (Port 6379) - Cache
- **pgAdmin** (Port 5050) - Database management GUI
- **Kafka** (Port 9092, 9094) - Event streaming platform
- **Kafka UI** (Port 8090) - Kafka monitoring and management

### 2. Start Application Services
```bash
docker-compose -f docker-compose-services.yml up -d
```

This starts your business services:
- **Gateway Service** (Port 8080)
- **Reservation Engine** (Port 8081)
- **Payment Processor** (Port 8084)

### 3. Start Everything Together
```bash
docker-compose -f docker-compose-infrastructure.yml -f docker-compose-services.yml up -d
```

## Development vs Production

### Development
- Use `docker-compose-infrastructure.yml` for external services
- Run your Java services locally with IDE for debugging
- Zipkin URL: `http://localhost:9411`

### Production
- Use both compose files
- Add proper resource limits
- Use environment-specific configurations

## Access Points

- **Zipkin UI**: http://localhost:9411
- **pgAdmin UI**: http://localhost:5050 (or http://172.27.108.197:5050 for WSL2)
- **Kafka UI**: http://localhost:8090 (or http://172.27.108.197:8090 for WSL2)
- **Database**: localhost:5432
- **Redis**: localhost:6379
- **Kafka**: localhost:9092 (internal), localhost:9094 (external)
- **Gateway**: http://localhost:8080

## Environment Variables

Create `.env` file in this directory:

```env
# Database
POSTGRES_DB=modern_reservation
POSTGRES_USER=dev_user
POSTGRES_PASSWORD=dev_password123

# Application
SPRING_PROFILES_ACTIVE=docker
ZIPKIN_BASE_URL=http://zipkin:9411
```

## Why This Approach?

1. **Separation of Concerns**: Infrastructure ≠ Application Code
2. **Easy Management**: Start/stop services independently
3. **Environment Consistency**: Same images across dev/staging/prod
4. **No Code Maintenance**: Use official, maintained images
5. **Scalability**: Easy to scale individual services

## Zipkin Configuration

Zipkin runs as a standalone service, not embedded in your application:

```yaml
# Your application just sends traces to Zipkin
spring:
  zipkin:
    base-url: http://zipkin:9411
  sleuth:
    zipkin:
      base-url: http://zipkin:9411
```

No need for custom Zipkin server code! 🎉
