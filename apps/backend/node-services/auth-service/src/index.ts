import Fastify from 'fastify';
import cors from '@fastify/cors';
import jwt from '@fastify/jwt';
import { config } from 'dotenv';
import { authRoutes } from './routes/auth.routes';
import { userRoutes } from './routes/user.routes';
import { healthRoutes } from './routes/health.routes';

config();

const server = Fastify({
  logger: {
    level: process.env.LOG_LEVEL || 'info',
    transport:
      process.env.NODE_ENV === 'development'
        ? {
            target: 'pino-pretty',
            options: {
              translateTime: 'HH:MM:ss Z',
              ignore: 'pid,hostname',
            },
          }
        : undefined,
  },
});

// Register plugins
server.register(cors, {
  origin: process.env.CORS_ORIGIN?.split(',') || [
    'http://localhost:3000',
    'http://localhost:3001',
    'http://localhost:8080',
  ],
  credentials: true,
});

server.register(jwt, {
  secret: process.env.JWT_SECRET || 'your-secret-key-change-in-production',
  sign: {
    expiresIn: '24h',
  },
});

// Register routes
server.register(healthRoutes, { prefix: '/health' });
server.register(authRoutes, { prefix: '/auth' });
server.register(userRoutes, { prefix: '/users' });

// Error handler
server.setErrorHandler((error, request, reply) => {
  server.log.error(error);
  reply.status(error.statusCode || 500).send({
    error: error.message || 'Internal Server Error',
    statusCode: error.statusCode || 500,
  });
});

// Start server
const start = async () => {
  try {
    const port = Number(process.env.PORT) || 3100;
    await server.listen({ port, host: '0.0.0.0' });
    server.log.info(`🔐 Auth service running on port ${port}`);

    // Register with Eureka if configured
    if (process.env.EUREKA_URL) {
      try {
        const { registerWithEureka } = await import('./utils/eureka.client');
        await registerWithEureka(port);
      } catch (err) {
        server.log.warn('Failed to register with Eureka, continuing without service discovery');
      }
    }
  } catch (err) {
    server.log.error(err);
    process.exit(1);
  }
};

start();
