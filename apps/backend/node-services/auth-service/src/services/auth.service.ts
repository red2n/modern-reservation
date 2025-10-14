import { FastifyInstance } from 'fastify';
import bcrypt from 'bcrypt';
import { UserSchema, type User } from '@modern-reservation/schemas';
import { UserRepository } from '../repositories/user.repository';

// Type definitions
interface UserInfo {
  id: string;
  email: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  role: string;
  tenantId: string;
  permissions: string[];
  properties?: string[];
  isActive?: boolean;
  lastLogin?: string;
}

interface AuthResponse {
  token: string;
  user: UserInfo;
}

// Helper function for permissions
const getPermissionsForRole = (role: string): string[] => {
  const rolePermissions: Record<string, string[]> = {
    GUEST: ['view_reservations', 'create_reservations'],
    FRONT_DESK: ['view_reservations', 'create_reservations', 'modify_reservations', 'check_in', 'check_out'],
    RESERVATION_MANAGER: ['view_reservations', 'create_reservations', 'modify_reservations', 'cancel_reservations'],
    HOTEL_ADMIN: ['view_reservations', 'create_reservations', 'modify_reservations', 'cancel_reservations', 'manage_rates'],
    FINANCE: ['view_payments', 'process_payments', 'view_reports'],
    HOUSEKEEPING: ['view_rooms', 'update_room_status'],
    MANAGER: ['full_access'],
  };
  return rolePermissions[role] || [];
};

export class AuthService {
  private userRepository: UserRepository;
  private server: FastifyInstance;

  constructor(server: FastifyInstance) {
    this.server = server;
    this.userRepository = new UserRepository();
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    // For demo purposes, handle demo accounts first
    const demoUsers = this.getDemoUsers();
    const demoUser = demoUsers[email];

    if (demoUser && password === 'demo123') {
      const token = this.server.jwt.sign({
        id: demoUser.id,
        email: demoUser.email,
        role: demoUser.role,
        tenantId: demoUser.tenantId,
        permissions: demoUser.permissions,
      });

      this.server.log.info({ email, role: demoUser.role }, 'Demo user logged in successfully');

      return {
        token,
        user: demoUser,
      };
    }

    // Check real database (when connected)
    try {
      const user = await this.userRepository.findByEmail(email);
      if (!user) {
        throw new Error('User not found');
      }

      const validPassword = await bcrypt.compare(password, user.passwordHash);
      if (!validPassword) {
        throw new Error('Invalid password');
      }

      const token = this.server.jwt.sign({
        id: user.id,
        email: user.email,
        role: user.role,
        tenantId: user.tenantId,
        permissions: user.permissions,
      });

      this.server.log.info({ userId: user.id, email: user.email }, 'User logged in successfully');

      return {
        token,
        user: {
          id: user.id,
          tenantId: user.tenantId,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          permissions: user.permissions,
          properties: user.properties || [],
          isActive: user.isActive,
          lastLogin: new Date().toISOString(),
        },
      };
    } catch (error) {
      // If database is not available and demo user exists, use demo
      if (demoUser && password === 'demo123') {
        const token = this.server.jwt.sign({
          id: demoUser.id,
          email: demoUser.email,
          role: demoUser.role,
          tenantId: demoUser.tenantId,
          permissions: demoUser.permissions,
        });

        this.server.log.warn(
          { email },
          'Database unavailable, using demo authentication fallback'
        );

        return {
          token,
          user: demoUser,
        };
      }
      throw error;
    }
  }

  private getDemoUsers(): Record<string, UserInfo> {
    return {
      'frontdesk@hotel.com': {
        id: 'fd001',
        tenantId: 'hotel_001',
        email: 'frontdesk@hotel.com',
        firstName: 'Sarah',
        lastName: 'Johnson',
        role: 'FRONT_DESK',
        permissions: getPermissionsForRole('FRONT_DESK'),
        properties: ['prop_001'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'reservations@hotel.com': {
        id: 'rm001',
        tenantId: 'hotel_001',
        email: 'reservations@hotel.com',
        firstName: 'Michael',
        lastName: 'Chen',
        role: 'RESERVATION_MANAGER',
        permissions: getPermissionsForRole('RESERVATION_MANAGER'),
        properties: ['prop_001'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'admin@hotel.com': {
        id: 'ha001',
        tenantId: 'hotel_001',
        email: 'admin@hotel.com',
        firstName: 'Emily',
        lastName: 'Rodriguez',
        role: 'HOTEL_ADMIN',
        permissions: getPermissionsForRole('HOTEL_ADMIN'),
        properties: ['prop_001'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'finance@hotel.com': {
        id: 'fn001',
        tenantId: 'hotel_001',
        email: 'finance@hotel.com',
        firstName: 'David',
        lastName: 'Thompson',
        role: 'FINANCE',
        permissions: getPermissionsForRole('FINANCE'),
        properties: ['prop_001'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'housekeeping@hotel.com': {
        id: 'hk001',
        tenantId: 'hotel_001',
        email: 'housekeeping@hotel.com',
        firstName: 'Maria',
        lastName: 'Garcia',
        role: 'HOUSEKEEPING',
        permissions: getPermissionsForRole('HOUSEKEEPING'),
        properties: ['prop_001'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'manager@hotel.com': {
        id: 'mg001',
        tenantId: 'hotel_001',
        email: 'manager@hotel.com',
        firstName: 'James',
        lastName: 'Wilson',
        role: 'MANAGER',
        permissions: getPermissionsForRole('MANAGER'),
        properties: ['prop_001', 'prop_002'],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
      'guest@example.com': {
        id: 'gt001',
        tenantId: 'hotel_001',
        email: 'guest@example.com',
        firstName: 'John',
        lastName: 'Smith',
        role: 'GUEST',
        permissions: getPermissionsForRole('GUEST'),
        properties: [],
        isActive: true,
        lastLogin: new Date().toISOString(),
      },
    };
  }
}
