/**
 * Role-Based Router Component
 * Single Responsibility: Route users to appropriate interfaces based on their role
 * Based on PRD user personas and access patterns
 */

'use client';

import React from 'react';
import { AdminDashboard } from '@/components/dashboards/AdminDashboard';
import { FinanceDashboard } from '@/components/dashboards/FinanceDashboard';
import { FrontDeskDashboard } from '@/components/dashboards/FrontDeskDashboard';
import { HousekeepingDashboard } from '@/components/dashboards/HousekeepingDashboard';
import { ManagerDashboard } from '@/components/dashboards/ManagerDashboard';
import { ReservationManagerDashboard } from '@/components/dashboards/ReservationManagerDashboard';
import { LoginPage } from '@/components/LoginPage';
// Import different dashboard components for each role
// These will be created in subsequent steps
import { GuestPortal } from '@/components/portals/GuestPortal';
import { type UserRole, useAuth } from '@/contexts/AuthContext';

interface RoleBasedRouterProps {
  className?: string;
}

export function RoleBasedRouter({ className }: RoleBasedRouterProps) {
  const { isAuthenticated, user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <LoginPage />;
  }

  // Route based on user role - matches PRD user personas
  const renderDashboard = () => {
    switch (user.role) {
      case 'GUEST':
        return <GuestPortal />;

      case 'FRONT_DESK':
        return <FrontDeskDashboard />;

      case 'RESERVATION_MANAGER':
        return <ReservationManagerDashboard />;

      case 'HOUSEKEEPING':
      case 'MAINTENANCE':
        return <HousekeepingDashboard />;

      case 'FINANCE':
        return <FinanceDashboard />;

      case 'HOTEL_ADMIN':
        return <AdminDashboard />;

      case 'MANAGER':
      case 'SYSTEM_ADMIN':
        return <ManagerDashboard />;

      default:
        // Fallback to guest portal for unknown roles
        return <GuestPortal />;
    }
  };

  return <div className={className}>{renderDashboard()}</div>;
}
