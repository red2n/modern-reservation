# Backend Infrastructure Services

This directory contains the core infrastructure services that provide the foundation for all microservices in the Modern Reservation System.

## 🏗️ Infrastructure Services Overview

### 1. Config Server (`config-server`)
**Port: 8888** | **Path: `/config`**

Centralized configuration management service using Spring Cloud Config.

**Features:**
- Git-based configuration repository support
- Environment-specific configurations (dev, staging, prod)
- Real-time configuration refresh
- Secure configuration access with basic authentication
- Health monitoring and metrics

**Endpoints:**
- Health: `http://localhost:8888/actuator/health`
- Config: `http://localhost:8888/config/{application}/{profile}`

### 2. Eureka Server (`eureka-server`)
**Port: 8761** | **Path: `/`**

Service discovery and registration server using Netflix Eureka.

**Features:**
- Service registration and discovery
- Health monitoring of registered services
- Load balancing support
- Failover and fault tolerance
- Web dashboard for service monitoring

**Endpoints:**
- Dashboard: `http://localhost:8761`
- Health: `http://localhost:8761/actuator/health`
- Registry: `http://localhost:8761/eureka/apps`

### 3. Gateway Service (`gateway-service`)
**Port: 8080** | **Path: `/`**

API Gateway service using Spring Cloud Gateway.

**Features:**
- Dynamic routing based on service discovery
- Load balancing across service instances
- Authentication and authorization
- Rate limiting and throttling
- Circuit breaker pattern
- CORS configuration
- Request/response transformation

**Endpoints:**
- Health: `http://localhost:8080/actuator/health`
- Routes: `http://localhost:8080/actuator/gateway/routes`
- Circuit Breakers: `http://localhost:8080/actuator/circuitbreakers`

### 4. Zipkin Server (`zipkin-server`)
**Port: 9411** | **Path: `/`**

Distributed tracing server for monitoring and debugging.

**Features:**
- Distributed request tracing
- Service dependency mapping
- Performance analysis and bottleneck identification
- Error tracking and debugging
- Interactive web UI for trace visualization

**Endpoints:**
- UI: `http://localhost:9411`
- Health: `http://localhost:9411/actuator/health`
- API: `http://localhost:9411/api/v2`

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Redis (for rate limiting in Gateway)
- PostgreSQL (for business services)

### Starting Infrastructure Services

1. **Start all services at once:**
   ```bash
   ./start-infrastructure.sh
   ```

2. **Start services individually:**
   ```bash
   # Config Server (must start first)
   cd infrastructure/config-server
   mvn spring-boot:run

   # Eureka Server
   cd infrastructure/eureka-server
   mvn spring-boot:run

   # Zipkin Server
   cd infrastructure/zipkin-server
   mvn spring-boot:run

   # Gateway Service
   cd infrastructure/gateway-service
   mvn spring-boot:run
   ```

### Stopping Infrastructure Services

```bash
./stop-infrastructure.sh
```

## 📊 Service Dependencies

```
┌─────────────────┐
│  Config Server  │ ← Must start first
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Eureka Server  │ ← Service Discovery
└─────────────────┘
         │
         ▼
┌─────────────────┐    ┌─────────────────┐
│  Zipkin Server  │    │  Gateway Service │ ← API Gateway
└─────────────────┘    └─────────────────┘
```

## 🔧 Configuration

### Config Server
- **Default Profile:** `native` (reads from classpath)
- **Configuration Location:** `src/main/resources/config/`
- **Security:** Basic auth (config-admin/admin123)

### Eureka Server
- **Self Registration:** Disabled
- **Self Preservation:** Disabled (development mode)
- **Renewal Threshold:** 85%

### Gateway Service
- **Rate Limiting:** Redis-based
- **Circuit Breaker:** Resilience4j
- **Security:** JWT-based authentication
- **CORS:** Configurable by environment

### Zipkin Server
- **Storage:** In-memory (development)
- **Sample Rate:** 100% (adjust for production)
- **UI:** Enabled by default

## 🔒 Security Configuration

### Default Credentials
- **Config Server:** `config-admin` / `admin123`
- **Eureka Server:** `eureka-admin` / `admin123`

### Environment Variables
Set these for production:
```bash
export CONFIG_SERVER_PASSWORD=your-secure-password
export EUREKA_PASSWORD=your-secure-password
export JWT_SECRET=your-jwt-secret-key
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
export REDIS_PASSWORD=your-redis-password
```

## 📈 Monitoring & Health Checks

All services expose Actuator endpoints:

### Health Checks
```bash
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Eureka Server
curl http://localhost:9411/actuator/health  # Zipkin Server
curl http://localhost:8080/actuator/health  # Gateway Service
```

### Metrics (Prometheus format)
```bash
curl http://localhost:8080/actuator/prometheus
```

### Service Discovery Status
```bash
curl http://localhost:8761/eureka/apps
```

## 🔄 Circuit Breaker Configuration

Gateway service includes circuit breakers for all downstream services:

- **Failure Rate Threshold:** 50% (30% for payments)
- **Wait Duration:** 10-15 seconds
- **Sliding Window:** 10 calls
- **Minimum Calls:** 3-5 calls

## 🌐 Routing Configuration

Gateway routes are configured for:
- **Java Services:** `/api/v1/{service}/**`
- **Node.js Services:** `/api/v1/{service}/**`
- **WebSocket:** `/ws/**`
- **GraphQL:** `/api/v1/graphql/**`

## 📝 Logs

Log files are created in each service's `logs/` directory:
- `config-server/logs/config-server.log`
- `eureka-server/logs/eureka-server.log`
- `gateway-service/logs/gateway-service.log`
- `zipkin-server/logs/zipkin-server.log`

## 🛠️ Development Tips

1. **Start Config Server first** - All other services depend on it
2. **Use Eureka Dashboard** to monitor service registration
3. **Check Zipkin UI** for request tracing and performance analysis
4. **Monitor Gateway metrics** for API usage patterns
5. **Configure Redis** for rate limiting to work properly

## 🚨 Troubleshooting

### Common Issues

1. **Service won't start:**
   - Check if required ports are available
   - Verify Java 21 is installed
   - Check Maven dependencies

2. **Services can't connect to Config Server:**
   - Ensure Config Server is running on port 8888
   - Verify credentials (config-admin/admin123)

3. **Services not registering with Eureka:**
   - Check Eureka Server is running on port 8761
   - Verify network connectivity
   - Check service application.yml configuration

4. **Gateway routing not working:**
   - Verify services are registered in Eureka
   - Check route configurations in application.yml
   - Test individual service endpoints

### Debug Commands

```bash
# Check running services
netstat -tlnp | grep -E "8080|8761|8888|9411"

# View service logs
tail -f infrastructure/config-server/logs/config-server.log
tail -f infrastructure/eureka-server/logs/eureka-server.log

# Test service connectivity
curl -f http://localhost:8761/eureka/apps
curl -f http://localhost:8080/actuator/gateway/routes
```

## 📚 Additional Resources

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Netflix Eureka Documentation](https://spring.io/guides/gs/service-registration-and-discovery)
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Zipkin Documentation](https://zipkin.io/)
