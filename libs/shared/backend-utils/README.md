# API Documentation with Swagger/OpenAPI 3.0

This document outlines the comprehensive API documentation setup for the Modern Reservation System using Swagger UI and OpenAPI 3.0 specifications.

## 🎯 Overview

Our API documentation strategy provides:
- **Interactive API Testing** with Swagger UI
- **Standardized Response Format** across all microservices
- **Comprehensive Error Handling** with detailed error codes
- **Automatic Documentation Generation** from code annotations
- **Multi-Environment Support** (dev, staging, production)
- **Security Documentation** for JWT authentication

## 📚 Implementation Details

### Dependencies Added

```xml
<!-- OpenAPI Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-annotations</artifactId>
    <version>2.2.17</version>
</dependency>
```

### Key Components Created

1. **OpenApiConfig.java** - Centralized OpenAPI configuration
2. **ApiResponse.java** - Standardized response wrapper
3. **ErrorDetails.java** - Structured error information
4. **GlobalExceptionHandler.java** - Consistent error handling
5. **HealthController.java** - Sample controller with full documentation

## 🚀 Usage Guide

### Accessing Swagger UI

Once any microservice is running, access the interactive documentation at:

```
# Development
http://localhost:8080/swagger-ui.html

# API JSON specification
http://localhost:8080/api-docs
```

### Service-Specific URLs

Each microservice will have its own Swagger UI:

```
# Gateway Service
http://localhost:8761/swagger-ui.html

# Reservation Engine
http://localhost:8082/swagger-ui.html

# Availability Calculator
http://localhost:8083/swagger-ui.html

# Rate Management
http://localhost:8084/swagger-ui.html

# Payment Processor
http://localhost:8085/swagger-ui.html
```

### Annotating Controllers

Use comprehensive Swagger annotations for all API endpoints:

```java
@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Hotel reservation management")
public class ReservationController {

    @Operation(
        summary = "Create new reservation",
        description = "Creates a new hotel reservation with guest and room details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Room not available")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationDto>> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        // Implementation
    }
}
```

### Request/Response DTOs

Document all DTOs with schema annotations:

```java
@Schema(description = "Reservation creation request")
public class CreateReservationRequest {

    @Schema(description = "Guest ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull
    private UUID guestId;

    @Schema(description = "Room ID", example = "room-001")
    @NotBlank
    private String roomId;

    @Schema(description = "Check-in date", example = "2025-10-01")
    @NotNull
    @Future
    private LocalDate checkInDate;

    // ... other fields
}
```

## 🔧 Configuration

### Application Properties

Add to each microservice's `application.yml`:

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    displayRequestDuration: true

app:
  version: "2.0.0"
  description: "Reservation Engine - Hotel booking and management"
```

### Security Configuration

JWT authentication is pre-configured in the OpenAPI setup:

```yaml
components:
  securitySchemes:
    Bearer Authentication:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

## 🌐 API Groups

Documentation is organized into logical groups:

- **Public API** (`/api/v1/public/**`) - Guest-facing endpoints
- **Admin API** (`/api/v1/admin/**`) - Administrative functions
- **Internal API** (`/api/v1/internal/**`) - Service-to-service communication

## 📊 Error Handling

All APIs return consistent error responses:

```json
{
  "status": "error",
  "code": 400,
  "message": "Validation failed",
  "error": {
    "errorCode": "VALIDATION_ERROR",
    "details": "Request validation failed",
    "fieldErrors": {
      "email": "Invalid email format",
      "checkInDate": "Must be a future date"
    }
  },
  "timestamp": "2025-09-25T10:30:00",
  "requestId": "req-123e4567-e89b-12d3-a456-426614174000"
}
```

## 🎨 UI Customization

Swagger UI includes:
- **Modern theme** with brand colors
- **Operation sorting** by HTTP method
- **Request duration display** for performance monitoring
- **Example values** for all request/response models
- **Interactive testing** with authentication support

## 🚀 Next Steps

1. **Add backend-utils dependency** to each microservice POM
2. **Configure application properties** for each service
3. **Import OpenApiConfig** in main application classes
4. **Start documenting APIs** with Swagger annotations
5. **Test interactive documentation** in Swagger UI

## 📝 Best Practices

- Use descriptive operation summaries and descriptions
- Provide realistic example values in schemas
- Document all possible response codes
- Include error scenarios in API responses
- Group related endpoints with tags
- Use consistent naming conventions

This comprehensive setup ensures that all APIs in the Modern Reservation System have professional, interactive documentation that supports both development and integration workflows.
