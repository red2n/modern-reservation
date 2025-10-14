/**
 * Main Dashboard Layout Component
 * Single Responsibility: Provide common layout structure for all dashboards
 * Implements the 18 core modules from PRD section 4.2
 */

'use client';

import {
  BellIcon,
  BookOpenIcon,
  BuildingOfficeIcon,
  CalendarIcon,
  ChartBarIcon,
  ChatBubbleLeftRightIcon,
  ClockIcon,
  Cog6ToothIcon,
  CogIcon,
  CreditCardIcon,
  CurrencyDollarIcon,
  DocumentArrowUpIcon,
  DocumentTextIcon,
  HomeIcon,
  KeyIcon,
  PhoneIcon,
  ShieldCheckIcon,
  UserGroupIcon
} from '@heroicons/react/24/outline';
import type React from 'react';
import { PERMISSIONS, useAuth } from '@/contexts/AuthContext';

// Module definitions based on PRD Section 4.2 - Core Features
export const DASHBOARD_MODULES = {
  // Core Operations
  RESERVATIONS: {
    id: 'reservations',
    title: 'Reservations',
    description: 'Manage bookings and reservations',
    icon: CalendarIcon,
    path: '/reservations',
    permission: PERMISSIONS.RESERVATIONS_VIEW,
  },
  AVAILABILITY: {
    id: 'availability',
    title: 'Room Availability',
    description: 'Track room inventory and availability',
    icon: HomeIcon,
    path: '/availability',
    permission: PERMISSIONS.AVAILABILITY_VIEW,
  },
  RATES: {
    id: 'rates',
    title: 'Rate Management',
    description: 'Configure pricing and rate plans',
    icon: CurrencyDollarIcon,
    path: '/rates',
    permission: PERMISSIONS.RATES_VIEW,
  },
  FRONT_DESK: {
    id: 'front-desk',
    title: 'Front Desk',
    description: 'Guest check-in/out operations',
    icon: KeyIcon,
    path: '/front-desk',
    permission: PERMISSIONS.CHECKIN_CHECKOUT,
  },

  // Guest Services
  HOUSEKEEPING: {
    id: 'housekeeping',
    title: 'Housekeeping',
    description: 'Room status and cleaning schedules',
    icon: ClockIcon,
    path: '/housekeeping',
    permission: PERMISSIONS.HOUSEKEEPING_VIEW,
  },
  MAINTENANCE: {
    id: 'maintenance',
    title: 'Maintenance',
    description: 'Property maintenance and repairs',
    icon: Cog6ToothIcon,
    path: '/maintenance',
    permission: PERMISSIONS.HOUSEKEEPING_MANAGE, // Maintenance uses housekeeping permissions for now
  },
  GUEST_SERVICES: {
    id: 'guest-services',
    title: 'Guest Services',
    description: 'Concierge and guest requests',
    icon: PhoneIcon,
    path: '/guest-services',
    permission: PERMISSIONS.GUEST_SERVICES,
  },

  // Financial Management
  BILLING: {
    id: 'billing',
    title: 'Billing & Payments',
    description: 'Process payments and manage billing',
    icon: CreditCardIcon,
    path: '/billing',
    permission: PERMISSIONS.BILLING_VIEW,
  },
  FINANCIAL_REPORTS: {
    id: 'financial-reports',
    title: 'Financial Reports',
    description: 'Revenue and financial analytics',
    icon: ChartBarIcon,
    path: '/reports/financial',
    permission: PERMISSIONS.BILLING_VIEW,
  },

  // Analytics & Reporting
  ANALYTICS: {
    id: 'analytics',
    title: 'Analytics Dashboard',
    description: 'Performance metrics and insights',
    icon: DocumentTextIcon,
    path: '/analytics',
    permission: PERMISSIONS.ANALYTICS_VIEW,
  },
  REPORTS: {
    id: 'reports',
    title: 'Reports',
    description: 'Generate operational reports',
    icon: DocumentArrowUpIcon,
    path: '/reports',
    permission: PERMISSIONS.REPORTS_VIEW,
  },

  // Administration
  USER_MANAGEMENT: {
    id: 'user-management',
    title: 'User Management',
    description: 'Manage staff and user accounts',
    icon: UserGroupIcon,
    path: '/admin/users',
    permission: PERMISSIONS.USERS_MANAGE,
  },
  PROPERTY_MANAGEMENT: {
    id: 'property-management',
    title: 'Property Management',
    description: 'Configure property settings',
    icon: BuildingOfficeIcon,
    path: '/admin/properties',
    permission: PERMISSIONS.SYSTEM_CONFIG,
  },
  SYSTEM_SETTINGS: {
    id: 'system-settings',
    title: 'System Settings',
    description: 'Configure system preferences',
    icon: CogIcon,
    path: '/admin/settings',
    permission: PERMISSIONS.SYSTEM_CONFIG,
  },

  // Communication & Notifications
  NOTIFICATIONS: {
    id: 'notifications',
    title: 'Notifications',
    description: 'System alerts and notifications',
    icon: BellIcon,
    path: '/notifications',
    permission: PERMISSIONS.RESERVATIONS_VIEW, // Use basic permission for notifications
  },
  COMMUNICATION: {
    id: 'communication',
    title: 'Communication Hub',
    description: 'Internal messaging and communication',
    icon: ChatBubbleLeftRightIcon,
    path: '/communication',
    permission: PERMISSIONS.GUEST_SERVICES,
  },

  // Security & Compliance
  AUDIT_LOGS: {
    id: 'audit-logs',
    title: 'Audit Logs',
    description: 'System activity and security logs',
    icon: ShieldCheckIcon,
    path: '/admin/audit',
    permission: PERMISSIONS.AUDIT_VIEW,
  },
  DOCUMENTATION: {
    id: 'documentation',
    title: 'Documentation',
    description: 'System guides and documentation',
    icon: BookOpenIcon,
    path: '/documentation',
    permission: PERMISSIONS.RESERVATIONS_VIEW, // Basic permission for documentation
  },
} as const;

export type ModuleId = keyof typeof DASHBOARD_MODULES;

interface DashboardLayoutProps {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  availableModules?: ModuleId[];
  className?: string;
}

interface NavigationSidebarProps {
  availableModules: ModuleId[];
}

function NavigationSidebar({ availableModules }: NavigationSidebarProps) {
  const { hasPermission } = useAuth();

  const filteredModules = availableModules
    .map(moduleId => DASHBOARD_MODULES[moduleId])
    .filter(module => hasPermission(module.permission));

  return (
    <div className="w-64 bg-white border-r border-gray-200 h-full">
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900">Modern Reservation</h2>
        <p className="text-sm text-gray-600">Management System</p>
      </div>

      <nav className="mt-6 px-3">
        <div className="space-y-1">
          {filteredModules.map((module) => {
            const IconComponent = module.icon;
            return (
              <a
                key={module.id}
                href={module.path}
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-50 hover:text-gray-900 group"
              >
                <IconComponent className="w-5 h-5 mr-3 text-gray-400 group-hover:text-gray-500" />
                <span>{module.title}</span>
              </a>
            );
          })}
        </div>
      </nav>
    </div>
  );
}

function DashboardHeader({ title, subtitle }: { title: string; subtitle?: string }) {
  const { user, logout } = useAuth();

  return (
    <div className="bg-white border-b border-gray-200">
      <div className="px-6 py-4">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
            {subtitle && <p className="text-gray-600">{subtitle}</p>}
          </div>

          <div className="flex items-center space-x-4">
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">{user?.firstName} {user?.lastName}</p>
              <p className="text-xs text-gray-600">{user?.email}</p>
              <p className="text-xs text-blue-600">{user?.role}</p>
            </div>

            <button
              onClick={logout}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Sign out
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export function DashboardLayout({
  title,
  subtitle,
  children,
  availableModules = [],
  className,
}: DashboardLayoutProps) {
  return (
    <div className={`min-h-screen bg-gray-50 flex ${className || ''}`}>
      <NavigationSidebar availableModules={availableModules} />

      <div className="flex-1 flex flex-col">
        <DashboardHeader title={title} subtitle={subtitle} />

        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
