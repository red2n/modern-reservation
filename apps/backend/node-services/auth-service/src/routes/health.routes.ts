import { FastifyPluginAsync } from 'fastify';

export const healthRoutes: FastifyPluginAsync = async (server) => {
  server.get('/', async (request, reply) => {
    return {
      status: 'UP',
      service: 'auth-service',
      timestamp: new Date().toISOString(),
      version: '1.0.0-alpha',
    };
  });

  server.get('/ready', async (request, reply) => {
    // Check if service is ready to handle requests
    return {
      ready: true,
      checks: {
        jwt: 'OK',
        cors: 'OK',
        database: 'NOT_CONNECTED', // TODO: Implement when database is integrated
      },
    };
  });

  server.get('/live', async (request, reply) => {
    return {
      alive: true,
      timestamp: new Date().toISOString(),
    };
  });
};
