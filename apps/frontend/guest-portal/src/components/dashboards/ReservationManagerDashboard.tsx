/**
 * Reservation Manager Dashboard Component
 * Single Responsibility: Interface for reservation management and analytics
 * Based on PRD Reservation Manager persona requirements
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function ReservationManagerDashboard() {
  const availableModules: ModuleId[] = [
    'RESERVATIONS',
    'AVAILABILITY',
    'RATES',
    'ANALYTICS',
    'REPORTS',
    'GUEST_SERVICES',
    'COMMUNICATION',
    'NOTIFICATIONS',
  ];

  return (
    <DashboardLayout
      title="Reservation Management"
      subtitle="Optimize occupancy and revenue performance"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {/* Revenue Analytics */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-2">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Revenue Performance</h3>
          <div className="grid grid-cols-3 gap-4 mb-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">$45,230</div>
              <p className="text-sm text-gray-600">This Month</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">$38,450</div>
              <p className="text-sm text-gray-600">Last Month</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">+17.6%</div>
              <p className="text-sm text-gray-600">Growth</p>
            </div>
          </div>
          <div className="h-40 bg-gray-50 rounded-md flex items-center justify-center">
            <p className="text-gray-500">Revenue Chart Placeholder</p>
          </div>
        </div>

        {/* Occupancy Forecast */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Occupancy Forecast</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Today</span>
              <div className="flex items-center">
                <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-12 h-2 bg-green-500 rounded-full"></div>
                </div>
                <span className="text-sm font-medium">78%</span>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Tomorrow</span>
              <div className="flex items-center">
                <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-14 h-2 bg-blue-500 rounded-full"></div>
                </div>
                <span className="text-sm font-medium">85%</span>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Weekend</span>
              <div className="flex items-center">
                <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-15 h-2 bg-purple-500 rounded-full"></div>
                </div>
                <span className="text-sm font-medium">95%</span>
              </div>
            </div>
          </div>
        </div>

        {/* Rate Strategy */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Rate Strategy</h3>
          <div className="space-y-4">
            <div className="p-3 bg-blue-50 rounded-md">
              <div className="flex justify-between items-center">
                <h4 className="font-medium text-blue-900">Standard Room</h4>
                <span className="text-blue-600 font-bold">$120</span>
              </div>
              <p className="text-sm text-blue-700 mt-1">Optimal pricing for current demand</p>
            </div>
            <div className="p-3 bg-yellow-50 rounded-md">
              <div className="flex justify-between items-center">
                <h4 className="font-medium text-yellow-900">Deluxe Suite</h4>
                <span className="text-yellow-600 font-bold">$200</span>
              </div>
              <p className="text-sm text-yellow-700 mt-1">Consider 5% increase for weekend</p>
            </div>
            <div className="p-3 bg-green-50 rounded-md">
              <div className="flex justify-between items-center">
                <h4 className="font-medium text-green-900">Presidential</h4>
                <span className="text-green-600 font-bold">$500</span>
              </div>
              <p className="text-sm text-green-700 mt-1">Premium positioning maintained</p>
            </div>
          </div>
        </div>

        {/* Booking Trends */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Booking Trends</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">Direct Bookings</span>
              <span className="font-medium text-green-600">65%</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">OTA Bookings</span>
              <span className="font-medium text-blue-600">25%</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">Corporate</span>
              <span className="font-medium text-purple-600">10%</span>
            </div>
          </div>
        </div>

        {/* Reservation Pipeline */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Reservation Pipeline</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
              <div>
                <div className="font-medium text-gray-900">Next 7 Days</div>
                <div className="text-sm text-gray-600">45 new reservations</div>
              </div>
              <div className="text-green-600 font-bold">+12%</div>
            </div>
            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
              <div>
                <div className="font-medium text-gray-900">Next 30 Days</div>
                <div className="text-sm text-gray-600">180 new reservations</div>
              </div>
              <div className="text-blue-600 font-bold">+8%</div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
