import { API } from '@modern-reservation/schemas';

export interface User {
  id: string;
  tenantId: string;
  email: string;
  firstName: string;
  lastName: string;
  passwordHash: string;
  role: API.UserRole;
  permissions: API.Permission[];
  properties?: string[];
  isActive: boolean;
  lastLogin?: string;
  createdAt: Date;
  updatedAt: Date;
}

export class UserRepository {
  // This will connect to the database when ready
  // For now, returning null to trigger demo fallback

  async findByEmail(email: string): Promise<User | null> {
    // TODO: Implement database connection with PostgreSQL
    // Example:
    // const result = await db.query('SELECT * FROM users WHERE email = $1', [email]);
    // return result.rows[0] || null;
    return null;
  }

  async findById(id: string): Promise<User | null> {
    // TODO: Implement database connection
    return null;
  }

  async create(user: Omit<User, 'id' | 'createdAt' | 'updatedAt'>): Promise<User> {
    // TODO: Implement database connection
    throw new Error('Not implemented - database integration pending');
  }

  async update(id: string, user: Partial<User>): Promise<User> {
    // TODO: Implement database connection
    throw new Error('Not implemented - database integration pending');
  }

  async updateLastLogin(id: string): Promise<void> {
    // TODO: Implement database connection
    // await db.query('UPDATE users SET last_login = NOW() WHERE id = $1', [id]);
  }
}
