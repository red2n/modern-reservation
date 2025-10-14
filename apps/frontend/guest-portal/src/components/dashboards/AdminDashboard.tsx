/**
 * Admin Dashboard Component
 * Single Responsibility: Interface for hotel administration and system management
 * Based on PRD Hotel Administrator persona requirements
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function AdminDashboard() {
  const availableModules: ModuleId[] = [
    'USER_MANAGEMENT',
    'PROPERTY_MANAGEMENT',
    'SYSTEM_SETTINGS',
    'AUDIT_LOGS',
    'ANALYTICS',
    'REPORTS',
    'RESERVATIONS',
    'HOUSEKEEPING',
    'MAINTENANCE',
    'COMMUNICATION',
    'NOTIFICATIONS',
  ];

  return (
    <DashboardLayout
      title="Hotel Administration"
      subtitle="System configuration and management"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {/* System Health */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">System Health</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">Database</div>
                <div className="text-sm text-green-700">All connections healthy</div>
              </div>
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            </div>
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">API Services</div>
                <div className="text-sm text-green-700">All services running</div>
              </div>
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            </div>
            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded">
              <div>
                <div className="font-medium text-yellow-900">Payment Gateway</div>
                <div className="text-sm text-yellow-700">Minor latency detected</div>
              </div>
              <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
            </div>
          </div>
        </div>

        {/* User Management Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">User Management</h3>
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center p-3 bg-blue-50 rounded">
              <div className="text-2xl font-bold text-blue-600">24</div>
              <p className="text-blue-700 text-sm">Active Staff</p>
            </div>
            <div className="text-center p-3 bg-green-50 rounded">
              <div className="text-2xl font-bold text-green-600">156</div>
              <p className="text-green-700 text-sm">Guest Users</p>
            </div>
            <div className="text-center p-3 bg-yellow-50 rounded">
              <div className="text-2xl font-bold text-yellow-600">3</div>
              <p className="text-yellow-700 text-sm">Pending</p>
            </div>
            <div className="text-center p-3 bg-red-50 rounded">
              <div className="text-2xl font-bold text-red-600">2</div>
              <p className="text-red-700 text-sm">Suspended</p>
            </div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Admin Activity</h3>
          <div className="space-y-3">
            <div className="flex items-center p-2 text-sm">
              <div className="w-2 h-2 bg-blue-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div>New user created: John Smith</div>
                <div className="text-gray-500">2 hours ago</div>
              </div>
            </div>
            <div className="flex items-center p-2 text-sm">
              <div className="w-2 h-2 bg-green-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div>Rate plan updated for Standard Room</div>
                <div className="text-gray-500">4 hours ago</div>
              </div>
            </div>
            <div className="flex items-center p-2 text-sm">
              <div className="w-2 h-2 bg-yellow-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div>System backup completed</div>
                <div className="text-gray-500">6 hours ago</div>
              </div>
            </div>
          </div>
        </div>

        {/* Property Settings */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Property Configuration</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center p-3 border rounded">
              <div>
                <div className="font-medium">Total Rooms</div>
                <div className="text-sm text-gray-600">Room inventory count</div>
              </div>
              <div className="text-xl font-bold text-blue-600">64</div>
            </div>
            <div className="flex justify-between items-center p-3 border rounded">
              <div>
                <div className="font-medium">Room Types</div>
                <div className="text-sm text-gray-600">Available categories</div>
              </div>
              <div className="text-xl font-bold text-green-600">3</div>
            </div>
            <button className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
              Manage Property Settings
            </button>
          </div>
        </div>

        {/* Security & Compliance */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Security Overview</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-sm">Failed Login Attempts (24h)</span>
              <span className="font-medium text-red-600">3</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm">Active Sessions</span>
              <span className="font-medium text-green-600">18</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm">Last Security Scan</span>
              <span className="font-medium text-gray-600">2 hours ago</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm">Compliance Status</span>
              <span className="font-medium text-green-600">Compliant</span>
            </div>
          </div>
        </div>

        {/* Backup & Maintenance */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">System Maintenance</h3>
          <div className="space-y-4">
            <div className="p-3 bg-green-50 rounded">
              <div className="flex justify-between items-center">
                <span className="font-medium text-green-900">Last Backup</span>
                <span className="text-sm text-green-700">Success</span>
              </div>
              <div className="text-sm text-green-700 mt-1">Today at 3:00 AM</div>
            </div>
            <div className="p-3 bg-blue-50 rounded">
              <div className="flex justify-between items-center">
                <span className="font-medium text-blue-900">Next Maintenance</span>
                <span className="text-sm text-blue-700">Scheduled</span>
              </div>
              <div className="text-sm text-blue-700 mt-1">Sunday at 2:00 AM</div>
            </div>
            <button className="w-full px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">
              View Maintenance Schedule
            </button>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
