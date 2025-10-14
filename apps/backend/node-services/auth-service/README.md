# Authentication Service (Node.js)

Lightweight authentication microservice built with Fastify for the Modern Reservation System.

## Overview

This service handles user authentication, JWT token generation, and authorization for the entire system. It's built with Node.js for optimal performance with high-concurrency authentication requests.

## Features

- ✅ JWT-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Permission-based authorization
- ✅ Demo user fallback for development
- ✅ Eureka service discovery integration
- ✅ Health check endpoints
- ✅ CORS support
- ✅ Structured logging with Pino

## Tech Stack

- **Fastify** - High-performance web framework
- **@fastify/jwt** - JWT authentication plugin
- **bcrypt** - Password hashing
- **Zod** - Schema validation (via shared schemas)
- **Pino** - Structured logging
- **TypeScript** - Type safety

## Setup

```bash
# Install dependencies
npm install

# Copy environment file
cp .env.example .env

# Run in development mode
npm run dev

# Build for production
npm run build

# Run production build
npm start
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `3100` |
| `JWT_SECRET` | Secret key for JWT signing | Required in production |
| `CORS_ORIGIN` | Allowed CORS origins (comma-separated) | `http://localhost:3000` |
| `EUREKA_URL` | Eureka server URL | `http://localhost:8761/eureka` |
| `DATABASE_URL` | PostgreSQL connection string | TBD |

## API Endpoints

### Authentication

- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh JWT token
- `POST /auth/logout` - User logout (audit log)
- `GET /auth/validate` - Validate JWT token

### User

- `GET /users/me` - Get current user profile
- `PUT /users/me` - Update current user profile
- `POST /users/change-password` - Change password

### Health

- `GET /health` - Health check
- `GET /health/ready` - Readiness probe
- `GET /health/live` - Liveness probe

## Demo Users

For development, the following demo users are available (password: `demo123`):

| Email | Role | Description |
|-------|------|-------------|
| `frontdesk@hotel.com` | FRONT_DESK | Front desk staff |
| `reservations@hotel.com` | RESERVATION_MANAGER | Reservation manager |
| `admin@hotel.com` | HOTEL_ADMIN | Hotel administrator |
| `finance@hotel.com` | FINANCE | Finance staff |
| `housekeeping@hotel.com` | HOUSEKEEPING | Housekeeping staff |
| `manager@hotel.com` | MANAGER | Property manager |
| `guest@example.com` | GUEST | Guest user |

## Integration

This service is registered with the API Gateway at `/auth/**` and Eureka service discovery as `AUTH-SERVICE`.

Frontend authentication should call:
```
http://localhost:8080/auth/login
```

Which will be routed through the Gateway to this service.

## Database Integration

Currently using demo user fallback. Database integration pending with PostgreSQL `users` table.

## Development

```bash
# Run with auto-reload
npm run dev

# Check code style
npm run check

# Fix code style
npm run fix

# Run tests
npm test
```

## Production Considerations

- [ ] Set strong `JWT_SECRET` in production
- [ ] Enable HTTPS
- [ ] Implement rate limiting
- [ ] Add request validation
- [ ] Implement refresh token rotation
- [ ] Add audit logging to database
- [ ] Implement account lockout after failed attempts
- [ ] Add two-factor authentication (2FA)
