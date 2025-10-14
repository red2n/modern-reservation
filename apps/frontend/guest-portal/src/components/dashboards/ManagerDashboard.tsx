/**
 * Manager Dashboard Component
 * Single Responsibility: Executive interface for property and system management
 * Based on PRD Property Manager and System Administrator personas
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function ManagerDashboard() {
  // Managers have access to all modules
  const availableModules: ModuleId[] = [
    'ANALYTICS',
    'REPORTS',
    'RESERVATIONS',
    'AVAILABILITY',
    'RATES',
    'FRONT_DESK',
    'HOUSEKEEPING',
    'MAINTENANCE',
    'GUEST_SERVICES',
    'BILLING',
    'FINANCIAL_REPORTS',
    'USER_MANAGEMENT',
    'PROPERTY_MANAGEMENT',
    'SYSTEM_SETTINGS',
    'NOTIFICATIONS',
    'COMMUNICATION',
    'AUDIT_LOGS',
    'DOCUMENTATION',
  ];

  return (
    <DashboardLayout
      title="Executive Dashboard"
      subtitle="Comprehensive property and business overview"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-4 gap-6">
        {/* Key Performance Indicators */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-4">
          <h3 className="text-lg font-medium text-gray-900 mb-6">Key Performance Indicators</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-4">
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">78%</div>
              <p className="text-sm text-green-700">Occupancy</p>
            </div>
            <div className="text-center p-4 bg-blue-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">$145</div>
              <p className="text-sm text-blue-700">ADR</p>
            </div>
            <div className="text-center p-4 bg-purple-50 rounded-lg">
              <div className="text-2xl font-bold text-purple-600">$113</div>
              <p className="text-sm text-purple-700">RevPAR</p>
            </div>
            <div className="text-center p-4 bg-indigo-50 rounded-lg">
              <div className="text-2xl font-bold text-indigo-600">4.7</div>
              <p className="text-sm text-indigo-700">Guest Rating</p>
            </div>
            <div className="text-center p-4 bg-yellow-50 rounded-lg">
              <div className="text-2xl font-bold text-yellow-600">$45K</div>
              <p className="text-sm text-yellow-700">Monthly Revenue</p>
            </div>
            <div className="text-center p-4 bg-pink-50 rounded-lg">
              <div className="text-2xl font-bold text-pink-600">92%</div>
              <p className="text-sm text-pink-700">Staff Efficiency</p>
            </div>
            <div className="text-center p-4 bg-teal-50 rounded-lg">
              <div className="text-2xl font-bold text-teal-600">15.3</div>
              <p className="text-sm text-teal-700">Avg LOS</p>
            </div>
            <div className="text-center p-4 bg-red-50 rounded-lg">
              <div className="text-2xl font-bold text-red-600">2.1%</div>
              <p className="text-sm text-red-700">Cancellation</p>
            </div>
          </div>
        </div>

        {/* Revenue Trends */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-2">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Revenue Trends</h3>
          <div className="h-64 bg-gray-50 rounded-md flex items-center justify-center mb-4">
            <p className="text-gray-500">Revenue Chart Placeholder</p>
          </div>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <div className="text-lg font-bold text-green-600">+12%</div>
              <p className="text-xs text-gray-600">vs Last Month</p>
            </div>
            <div>
              <div className="text-lg font-bold text-blue-600">+18%</div>
              <p className="text-xs text-gray-600">vs Last Year</p>
            </div>
            <div>
              <div className="text-lg font-bold text-purple-600">$432K</div>
              <p className="text-xs text-gray-600">YTD Total</p>
            </div>
          </div>
        </div>

        {/* Operational Metrics */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-2">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Operational Status</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">Front Desk Operations</div>
                <div className="text-sm text-green-700">All stations active</div>
              </div>
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            </div>
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">Housekeeping</div>
                <div className="text-sm text-green-700">On schedule - 92% efficiency</div>
              </div>
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            </div>
            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded">
              <div>
                <div className="font-medium text-yellow-900">Maintenance</div>
                <div className="text-sm text-yellow-700">3 pending requests</div>
              </div>
              <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
            </div>
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">Payment Processing</div>
                <div className="text-sm text-green-700">All systems operational</div>
              </div>
              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            </div>
          </div>
        </div>

        {/* Guest Satisfaction */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Guest Satisfaction</h3>
          <div className="space-y-4">
            <div className="text-center mb-4">
              <div className="text-4xl font-bold text-green-600">4.7/5</div>
              <p className="text-gray-600">Average Rating</p>
              <p className="text-sm text-gray-500">Based on 156 reviews this month</p>
            </div>
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <span className="text-sm">Service</span>
                <div className="flex items-center">
                  <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                    <div className="w-15 h-2 bg-green-500 rounded-full"></div>
                  </div>
                  <span className="text-sm">4.8</span>
                </div>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm">Cleanliness</span>
                <div className="flex items-center">
                  <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                    <div className="w-14 h-2 bg-green-500 rounded-full"></div>
                  </div>
                  <span className="text-sm">4.6</span>
                </div>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm">Value</span>
                <div className="flex items-center">
                  <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                    <div className="w-13 h-2 bg-blue-500 rounded-full"></div>
                  </div>
                  <span className="text-sm">4.4</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Staff Performance */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Staff Performance</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-2">
              <div>
                <div className="font-medium">Front Desk</div>
                <div className="text-sm text-gray-600">8 active staff</div>
              </div>
              <div className="text-green-600 font-medium">96%</div>
            </div>
            <div className="flex justify-between items-center p-2">
              <div>
                <div className="font-medium">Housekeeping</div>
                <div className="text-sm text-gray-600">12 active staff</div>
              </div>
              <div className="text-green-600 font-medium">92%</div>
            </div>
            <div className="flex justify-between items-center p-2">
              <div>
                <div className="font-medium">Maintenance</div>
                <div className="text-sm text-gray-600">4 active staff</div>
              </div>
              <div className="text-yellow-600 font-medium">88%</div>
            </div>
          </div>
        </div>

        {/* Upcoming Events */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Upcoming Events</h3>
          <div className="space-y-3">
            <div className="p-3 bg-blue-50 rounded border-l-4 border-blue-500">
              <div className="font-medium text-blue-900">Board Meeting</div>
              <div className="text-sm text-blue-700">Tomorrow at 2:00 PM</div>
            </div>
            <div className="p-3 bg-green-50 rounded border-l-4 border-green-500">
              <div className="font-medium text-green-900">Staff Training</div>
              <div className="text-sm text-green-700">Friday at 10:00 AM</div>
            </div>
            <div className="p-3 bg-purple-50 rounded border-l-4 border-purple-500">
              <div className="font-medium text-purple-900">System Maintenance</div>
              <div className="text-sm text-purple-700">Sunday at 2:00 AM</div>
            </div>
          </div>
        </div>

        {/* Critical Alerts */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Critical Alerts</h3>
          <div className="space-y-3">
            <div className="flex items-center p-3 bg-red-50 rounded border-l-4 border-red-500">
              <div className="flex-1">
                <div className="font-medium text-red-900">Payment Gateway Issue</div>
                <div className="text-sm text-red-700">Minor latency detected</div>
              </div>
              <div className="text-red-600 text-xs">2h ago</div>
            </div>
            <div className="flex items-center p-3 bg-yellow-50 rounded border-l-4 border-yellow-500">
              <div className="flex-1">
                <div className="font-medium text-yellow-900">Room 403 Out of Service</div>
                <div className="text-sm text-yellow-700">AC repair in progress</div>
              </div>
              <div className="text-yellow-600 text-xs">4h ago</div>
            </div>
            <div className="p-3 text-center text-gray-500">
              <div className="text-sm">All other systems operational ✓</div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
