/**
 * Front Desk Dashboard Component
 * Single Responsibility: Interface for front desk operations
 * Based on PRD Front Desk Staff persona requirements
 */

'use client';

import React from 'react';
import { DASHBOARD_MODULES, DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function FrontDeskDashboard() {
  // Modules available to Front Desk Staff based on their role
  const availableModules: ModuleId[] = [
    'RESERVATIONS',
    'AVAILABILITY',
    'FRONT_DESK',
    'GUEST_SERVICES',
    'BILLING',
    'HOUSEKEEPING', // View-only for room status
    'COMMUNICATION',
    'NOTIFICATIONS',
  ];

  return (
    <DashboardLayout
      title="Front Desk Operations"
      subtitle="Guest services and daily operations"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Quick Actions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
          <div className="space-y-3">
            <button className="w-full text-left px-4 py-2 bg-blue-50 hover:bg-blue-100 rounded-md text-blue-700 font-medium">
              Check In Guest
            </button>
            <button className="w-full text-left px-4 py-2 bg-green-50 hover:bg-green-100 rounded-md text-green-700 font-medium">
              Check Out Guest
            </button>
            <button className="w-full text-left px-4 py-2 bg-yellow-50 hover:bg-yellow-100 rounded-md text-yellow-700 font-medium">
              Walk-in Registration
            </button>
            <button className="w-full text-left px-4 py-2 bg-purple-50 hover:bg-purple-100 rounded-md text-purple-700 font-medium">
              Room Change Request
            </button>
          </div>
        </div>

        {/* Today's Arrivals */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Today's Arrivals</h3>
          <div className="text-center py-4">
            <div className="text-3xl font-bold text-blue-600">12</div>
            <p className="text-gray-600">Expected Check-ins</p>
          </div>
          <button className="w-full mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-medium">
            View All Arrivals
          </button>
        </div>

        {/* Today's Departures */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Today's Departures</h3>
          <div className="text-center py-4">
            <div className="text-3xl font-bold text-green-600">8</div>
            <p className="text-gray-600">Expected Check-outs</p>
          </div>
          <button className="w-full mt-4 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-md font-medium">
            View All Departures
          </button>
        </div>

        {/* Room Status Overview */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Room Status</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div className="text-center p-3 bg-green-50 rounded-md">
              <div className="text-2xl font-bold text-green-600">45</div>
              <p className="text-green-700">Clean & Ready</p>
            </div>
            <div className="text-center p-3 bg-yellow-50 rounded-md">
              <div className="text-2xl font-bold text-yellow-600">8</div>
              <p className="text-yellow-700">Cleaning</p>
            </div>
            <div className="text-center p-3 bg-red-50 rounded-md">
              <div className="text-2xl font-bold text-red-600">2</div>
              <p className="text-red-700">Out of Order</p>
            </div>
            <div className="text-center p-3 bg-blue-50 rounded-md">
              <div className="text-2xl font-bold text-blue-600">48</div>
              <p className="text-blue-700">Occupied</p>
            </div>
          </div>
        </div>

        {/* Pending Requests */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Pending Requests</h3>
          <div className="space-y-2">
            <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
              <span className="text-sm">Room 201 - Extra Towels</span>
              <span className="text-xs text-gray-500">10:30 AM</span>
            </div>
            <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
              <span className="text-sm">Room 315 - Late Checkout</span>
              <span className="text-xs text-gray-500">11:15 AM</span>
            </div>
            <div className="flex justify-between items-center p-2 bg-gray-50 rounded">
              <span className="text-sm">Room 102 - Maintenance</span>
              <span className="text-xs text-gray-500">9:45 AM</span>
            </div>
          </div>
        </div>

        {/* Current Occupancy */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Occupancy Overview</h3>
          <div className="text-center py-4">
            <div className="text-3xl font-bold text-indigo-600">78%</div>
            <p className="text-gray-600">Current Occupancy</p>
            <p className="text-sm text-gray-500 mt-2">48 of 62 rooms occupied</p>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
