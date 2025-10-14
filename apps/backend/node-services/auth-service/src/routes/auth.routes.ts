import { FastifyPluginAsync } from 'fastify';
import { API } from '@modern-reservation/schemas';
import { AuthService } from '../services/auth.service';

export const authRoutes: FastifyPluginAsync = async (server) => {
  const authService = new AuthService(server);

  // Login endpoint
  server.post<{
    Body: API.AuthRequest;
  }>('/login', async (request, reply) => {
    const { email, password } = request.body;

    try {
      const result = await authService.login(email, password);
      return reply.send(result);
    } catch (error) {
      server.log.error({ error, email }, 'Login failed');
      return reply.status(401).send({
        error: 'Invalid credentials',
        message: error instanceof Error ? error.message : 'Authentication failed',
      });
    }
  });

  // Refresh token endpoint
  server.post('/refresh', async (request, reply) => {
    try {
      await request.jwtVerify();
      const user = request.user as any;
      const newToken = server.jwt.sign({
        id: user.id,
        email: user.email,
        role: user.role,
        tenantId: user.tenantId,
        permissions: user.permissions,
      });

      return reply.send({
        token: newToken,
        expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      });
    } catch (error) {
      return reply.status(401).send({ error: 'Invalid token' });
    }
  });

  // Logout endpoint (for audit logging)
  server.post('/logout', async (request, reply) => {
    try {
      await request.jwtVerify();
      const user = request.user as any;
      server.log.info({ userId: user.id, email: user.email }, 'User logged out');
      return reply.send({ success: true, message: 'Logged out successfully' });
    } catch (error) {
      // Even if token is invalid, consider logout successful
      return reply.send({ success: true, message: 'Logged out successfully' });
    }
  });

  // Validate token endpoint
  server.get('/validate', async (request, reply) => {
    try {
      await request.jwtVerify();
      return reply.send({
        valid: true,
        user: request.user,
      });
    } catch (error) {
      return reply.status(401).send({
        valid: false,
        error: 'Invalid token',
      });
    }
  });
};
