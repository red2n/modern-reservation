/**
 * Housekeeping Dashboard Component
 * Single Responsibility: Interface for housekeeping and maintenance operations
 * Based on PRD Housekeeping Staff and Maintenance personas
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function HousekeepingDashboard() {
  const availableModules: ModuleId[] = [
    'HOUSEKEEPING',
    'MAINTENANCE',
    'FRONT_DESK', // View-only for room status
    'COMMUNICATION',
    'NOTIFICATIONS',
  ];

  return (
    <DashboardLayout
      title="Housekeeping & Maintenance"
      subtitle="Room care and facility management"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {/* Room Status Grid */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-2">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Room Status Overview</h3>
          <div className="grid grid-cols-8 gap-2">
            {Array.from({ length: 64 }, (_, i) => {
              const roomNumber = 100 + i + 1;
              const statuses = ['clean', 'dirty', 'cleaning', 'maintenance', 'ooo'] as const;
              const status = statuses[Math.floor(Math.random() * statuses.length)];

              const statusColors: Record<typeof status, string> = {
                clean: 'bg-green-200 text-green-800',
                dirty: 'bg-red-200 text-red-800',
                cleaning: 'bg-yellow-200 text-yellow-800',
                maintenance: 'bg-orange-200 text-orange-800',
                ooo: 'bg-gray-200 text-gray-800',
              };

              return (
                <div
                  key={roomNumber}
                  className={`p-2 rounded text-xs text-center font-medium cursor-pointer hover:opacity-80 ${statusColors[status]}`}
                >
                  {roomNumber}
                </div>
              );
            })}
          </div>
          <div className="mt-4 flex flex-wrap gap-4 text-sm">
            <div className="flex items-center">
              <div className="w-3 h-3 bg-green-200 rounded mr-2"></div>
              <span>Clean & Ready</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 bg-red-200 rounded mr-2"></div>
              <span>Dirty</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 bg-yellow-200 rounded mr-2"></div>
              <span>In Progress</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 bg-orange-200 rounded mr-2"></div>
              <span>Maintenance</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 bg-gray-200 rounded mr-2"></div>
              <span>Out of Order</span>
            </div>
          </div>
        </div>

        {/* Daily Task Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Today's Tasks</h3>
          <div className="space-y-4">
            <div className="p-3 bg-green-50 rounded-md">
              <div className="text-2xl font-bold text-green-600">18</div>
              <p className="text-green-700 font-medium">Completed</p>
            </div>
            <div className="p-3 bg-yellow-50 rounded-md">
              <div className="text-2xl font-bold text-yellow-600">12</div>
              <p className="text-yellow-700 font-medium">In Progress</p>
            </div>
            <div className="p-3 bg-red-50 rounded-md">
              <div className="text-2xl font-bold text-red-600">8</div>
              <p className="text-red-700 font-medium">Pending</p>
            </div>
          </div>
        </div>

        {/* Staff Assignment */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Staff Assignments</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
              <div>
                <div className="font-medium">Maria Garcia</div>
                <div className="text-sm text-gray-600">Floors 1-2 (Rooms 101-216)</div>
              </div>
              <div className="text-green-600 text-sm font-medium">On Time</div>
            </div>
            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
              <div>
                <div className="font-medium">James Wilson</div>
                <div className="text-sm text-gray-600">Floors 3-4 (Rooms 301-416)</div>
              </div>
              <div className="text-yellow-600 text-sm font-medium">Delayed</div>
            </div>
            <div className="flex justify-between items-center p-3 bg-gray-50 rounded">
              <div>
                <div className="font-medium">Lisa Chen</div>
                <div className="text-sm text-gray-600">Common Areas & Lobby</div>
              </div>
              <div className="text-green-600 text-sm font-medium">Ahead</div>
            </div>
          </div>
        </div>

        {/* Priority Rooms */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Priority Cleaning</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-3 border-l-4 border-red-500 bg-red-50">
              <div>
                <div className="font-medium text-red-900">Room 205</div>
                <div className="text-sm text-red-700">VIP arrival at 2:00 PM</div>
              </div>
              <div className="text-red-600 text-xs font-medium">URGENT</div>
            </div>
            <div className="flex justify-between items-center p-3 border-l-4 border-yellow-500 bg-yellow-50">
              <div>
                <div className="font-medium text-yellow-900">Room 312</div>
                <div className="text-sm text-yellow-700">Early check-in requested</div>
              </div>
              <div className="text-yellow-600 text-xs font-medium">HIGH</div>
            </div>
            <div className="flex justify-between items-center p-3 border-l-4 border-blue-500 bg-blue-50">
              <div>
                <div className="font-medium text-blue-900">Room 108</div>
                <div className="text-sm text-blue-700">Late checkout completed</div>
              </div>
              <div className="text-blue-600 text-xs font-medium">NORMAL</div>
            </div>
          </div>
        </div>

        {/* Maintenance Requests */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Maintenance Requests</h3>
          <div className="space-y-3">
            <div className="p-3 bg-gray-50 rounded">
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-medium">Room 403 - AC Issue</div>
                  <div className="text-sm text-gray-600">Reported: 9:30 AM</div>
                </div>
                <span className="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">Critical</span>
              </div>
              <p className="text-sm text-gray-700 mt-2">Air conditioning not cooling properly</p>
            </div>
            <div className="p-3 bg-gray-50 rounded">
              <div className="flex justify-between items-start">
                <div>
                  <div className="font-medium">Room 201 - Plumbing</div>
                  <div className="text-sm text-gray-600">Reported: 10:15 AM</div>
                </div>
                <span className="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded">
                  Medium
                </span>
              </div>
              <p className="text-sm text-gray-700 mt-2">Slow draining bathroom sink</p>
            </div>
          </div>
        </div>

        {/* Supply Inventory */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Supply Status</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-sm">Towels</span>
              <div className="flex items-center">
                <div className="w-20 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-16 h-2 bg-green-500 rounded-full"></div>
                </div>
                <span className="text-sm text-green-600">Good</span>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm">Bed Linens</span>
              <div className="flex items-center">
                <div className="w-20 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-10 h-2 bg-yellow-500 rounded-full"></div>
                </div>
                <span className="text-sm text-yellow-600">Low</span>
              </div>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm">Toiletries</span>
              <div className="flex items-center">
                <div className="w-20 h-2 bg-gray-200 rounded-full mr-2">
                  <div className="w-4 h-2 bg-red-500 rounded-full"></div>
                </div>
                <span className="text-sm text-red-600">Critical</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
