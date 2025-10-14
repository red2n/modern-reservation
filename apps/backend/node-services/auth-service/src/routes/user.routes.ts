import { FastifyPluginAsync } from 'fastify';

export const userRoutes: FastifyPluginAsync = async (server) => {
  // Get current user
  server.get('/me', async (request, reply) => {
    try {
      await request.jwtVerify();
      return request.user;
    } catch (error) {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
  });

  // Update current user (stub - to be implemented)
  server.put('/me', async (request, reply) => {
    try {
      await request.jwtVerify();
      // TODO: Implement user update logic
      return {
        message: 'User update not implemented yet',
        user: request.user,
      };
    } catch (error) {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
  });

  // Change password (stub - to be implemented)
  server.post('/change-password', async (request, reply) => {
    try {
      await request.jwtVerify();
      // TODO: Implement password change logic
      return {
        message: 'Password change not implemented yet',
      };
    } catch (error) {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
  });
};
