/**
 * Authentication Context
 * Single Responsibility: Manage user authentication state and role-based access control
 * Based on PRD Section 3 - Target Users & Personas
 */

'use client';

import React, { createContext, type ReactNode, useContext, useEffect, useState } from 'react';
import { API } from '@modern-reservation/schemas';
import {
  getPermissionsForRole,
  hasPermission,
  hasAnyPermission
} from '@modern-reservation/schemas/dist/api/auth';

// Use shared schema types
export type UserRole = API.UserRole;
export type User = API.UserInfo;
export type Permission = API.Permission;

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  token: string | null;
}

export interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  hasPermission: (permission: Permission) => boolean;
  hasRole: (roles: UserRole | UserRole[]) => boolean;
  canAccessModule: (module: string) => boolean;
  switchProperty: (propertyId: string) => Promise<void>;
  currentProperty: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);



// Demo authentication function - maps demo credentials to user objects
const mockAuthentication = async (email: string, password: string): Promise<User | null> => {
  // Simulate API delay
  await new Promise(resolve => setTimeout(resolve, 800));

  // Demo credentials mapping
  const demoUsers: Record<string, User> = {
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

  // Check if credentials are valid (for demo, password must be 'demo123')
  if (password !== 'demo123') {
    return null;
  }

  return demoUsers[email] || null;
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authState, setAuthState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    token: null,
  });

  const [currentProperty, setCurrentProperty] = useState<string | null>(null);

  useEffect(() => {
    // Check for existing session
    const token = localStorage.getItem('auth_token');
    const userData = localStorage.getItem('user_data');

    if (token && userData) {
      try {
        const user = JSON.parse(userData);
        setAuthState({
          user,
          isAuthenticated: true,
          isLoading: false,
          token,
        });

        // Set default property
        if (user.properties.length > 0) {
          setCurrentProperty(user.properties[0]);
        }
      } catch (error) {
        console.error('Error parsing stored user data:', error);
        logout();
      }
    } else {
      setAuthState((prev) => ({ ...prev, isLoading: false }));
    }
  }, []);

  const login = async (email: string, password: string): Promise<void> => {
    setAuthState((prev) => ({ ...prev, isLoading: true }));

    try {
      // Call authentication API through Gateway Service
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        // Fallback to demo authentication for development
        const user = await mockAuthentication(email, password);
        if (!user) {
          throw new Error('Invalid credentials');
        }

        // Generate mock token
        const token = `demo_token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

        // Store authentication data
        localStorage.setItem('auth_token', token);
        localStorage.setItem('user_data', JSON.stringify(user));

        setAuthState({
          user,
          isAuthenticated: true,
          isLoading: false,
          token,
        });

        // Set default property
        if (user.properties.length > 0) {
          setCurrentProperty(user.properties[0]);
        }
        return;
      }

      // Handle successful API response
      const authData = await response.json();
      const { user, token } = authData;

      // Store authentication data
      localStorage.setItem('auth_token', token);
      localStorage.setItem('user_data', JSON.stringify(user));

      setAuthState({
        user,
        isAuthenticated: true,
        isLoading: false,
        token,
      });

      // Set default property
      if (user.properties.length > 0) {
        setCurrentProperty(user.properties[0]);
      }
    } catch (error) {
      setAuthState({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        token: null,
      });
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_data');
    setAuthState({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      token: null,
    });
    setCurrentProperty(null);
  };

  const hasPermissionCheck = (permission: Permission): boolean => {
    if (!authState.user) return false;
    return hasPermission(authState.user.permissions, permission);
  };

  const hasRole = (roles: UserRole | UserRole[]): boolean => {
    if (!authState.user) return false;

    const roleArray = Array.isArray(roles) ? roles : [roles];
    return roleArray.includes(authState.user.role);
  };

  const canAccessModule = (module: string): boolean => {
    if (!authState.user) return false;

    // Module access logic based on PRD requirements
    const modulePermissions: Record<string, Permission[]> = {
      reservations: ['RESERVATIONS_VIEW'],
      availability: ['AVAILABILITY_VIEW'],
      rates: ['RATES_VIEW'],
      'front-desk': ['CHECKIN_CHECKOUT'],
      housekeeping: ['HOUSEKEEPING_VIEW'],
      reports: ['REPORTS_VIEW'],
      billing: ['REPORTS_VIEW'], // Using available permission
      admin: ['USERS_MANAGE', 'SYSTEM_CONFIG'],
    };

    const requiredPermissions = modulePermissions[module] || [];
    return hasAnyPermission(authState.user.permissions, requiredPermissions);
  };

  const switchProperty = async (propertyId: string): Promise<void> => {
    if (!authState.user?.properties.includes(propertyId)) {
      throw new Error('User does not have access to this property');
    }

    setCurrentProperty(propertyId);

    // TODO: Update user session with new property context
    // This would typically involve an API call to update the session
  };

  const contextValue: AuthContextType = {
    ...authState,
    login,
    logout,
    hasPermission: hasPermissionCheck,
    hasRole,
    canAccessModule,
    switchProperty,
    currentProperty,
  };

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

// Mock login for development - TODO: Remove in production
export const mockLogin = (role: UserRole = 'FRONT_DESK'): User => ({
  id: 'mock-user-id',
  tenantId: 'tenant-123',
  email: `${role.toLowerCase()}@hotel.com`,
  firstName: 'Test',
  lastName: 'User',
  role,
  permissions: getPermissionsForRole(role),
  properties: ['property-123', 'property-456'],
  isActive: true,
  lastLogin: new Date().toISOString(),
});
