/**
 * Finance Dashboard Component
 * Single Responsibility: Interface for financial management and billing operations
 * Based on PRD Finance Team persona requirements
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function FinanceDashboard() {
  const availableModules: ModuleId[] = [
    'BILLING',
    'FINANCIAL_REPORTS',
    'ANALYTICS',
    'RESERVATIONS', // View-only for billing purposes
    'REPORTS',
    'AUDIT_LOGS',
    'NOTIFICATIONS',
  ];

  return (
    <DashboardLayout
      title="Financial Management"
      subtitle="Revenue tracking and billing operations"
      availableModules={availableModules}
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {/* Revenue Overview */}
        <div className="bg-white rounded-lg shadow p-6 xl:col-span-2">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Revenue Overview</h3>
          <div className="grid grid-cols-4 gap-4 mb-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">$45,230</div>
              <p className="text-sm text-gray-600">This Month</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">$38,450</div>
              <p className="text-sm text-gray-600">Last Month</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">$432,180</div>
              <p className="text-sm text-gray-600">YTD Revenue</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-indigo-600">+17.6%</div>
              <p className="text-sm text-gray-600">Growth</p>
            </div>
          </div>
          <div className="h-40 bg-gray-50 rounded-md flex items-center justify-center">
            <p className="text-gray-500">Revenue Trend Chart Placeholder</p>
          </div>
        </div>

        {/* Payment Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Payment Summary</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center p-3 bg-green-50 rounded">
              <div>
                <div className="font-medium text-green-900">Processed Today</div>
                <div className="text-sm text-green-700">42 transactions</div>
              </div>
              <div className="text-green-600 font-bold">$8,450</div>
            </div>
            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded">
              <div>
                <div className="font-medium text-yellow-900">Pending</div>
                <div className="text-sm text-yellow-700">5 transactions</div>
              </div>
              <div className="text-yellow-600 font-bold">$1,240</div>
            </div>
            <div className="flex justify-between items-center p-3 bg-red-50 rounded">
              <div>
                <div className="font-medium text-red-900">Failed</div>
                <div className="text-sm text-red-700">2 transactions</div>
              </div>
              <div className="text-red-600 font-bold">$320</div>
            </div>
          </div>
        </div>

        {/* Outstanding Balances */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Outstanding Balances</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2 border-b">
              <div>
                <div className="font-medium">Current (0-30 days)</div>
                <div className="text-sm text-gray-600">12 accounts</div>
              </div>
              <div className="text-green-600 font-bold">$2,450</div>
            </div>
            <div className="flex justify-between items-center py-2 border-b">
              <div>
                <div className="font-medium">30-60 days</div>
                <div className="text-sm text-gray-600">3 accounts</div>
              </div>
              <div className="text-yellow-600 font-bold">$890</div>
            </div>
            <div className="flex justify-between items-center py-2">
              <div>
                <div className="font-medium">60+ days</div>
                <div className="text-sm text-gray-600">1 account</div>
              </div>
              <div className="text-red-600 font-bold">$320</div>
            </div>
          </div>
        </div>

        {/* Daily Financial Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Daily Summary</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Room Revenue</span>
              <span className="font-medium">$7,230</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Food & Beverage</span>
              <span className="font-medium">$1,450</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Services</span>
              <span className="font-medium">$320</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">Taxes</span>
              <span className="font-medium">$890</span>
            </div>
            <div className="border-t pt-2">
              <div className="flex justify-between items-center font-bold">
                <span>Total</span>
                <span className="text-green-600">$9,890</span>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Transactions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Transactions</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center p-2 border-b">
              <div>
                <div className="font-medium">Room 205 - Smith, J.</div>
                <div className="text-sm text-gray-600">Credit Card - •••• 4242</div>
              </div>
              <div className="text-right">
                <div className="font-bold text-green-600">$450.00</div>
                <div className="text-xs text-gray-500">10:30 AM</div>
              </div>
            </div>
            <div className="flex justify-between items-center p-2 border-b">
              <div>
                <div className="font-medium">Room 312 - Johnson, M.</div>
                <div className="text-sm text-gray-600">Debit Card - •••• 8765</div>
              </div>
              <div className="text-right">
                <div className="font-bold text-green-600">$280.00</div>
                <div className="text-xs text-gray-500">9:45 AM</div>
              </div>
            </div>
            <div className="flex justify-between items-center p-2">
              <div>
                <div className="font-medium">Room 108 - Davis, R.</div>
                <div className="text-sm text-red-600">Payment Failed</div>
              </div>
              <div className="text-right">
                <div className="font-bold text-red-600">$320.00</div>
                <div className="text-xs text-gray-500">9:15 AM</div>
              </div>
            </div>
          </div>
        </div>

        {/* Tax Summary */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Tax Summary (Current Month)</h3>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">Occupancy Tax</span>
              <span className="font-medium">$2,450</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">City Tax</span>
              <span className="font-medium">$1,230</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-600">Service Tax</span>
              <span className="font-medium">$890</span>
            </div>
            <div className="border-t pt-2">
              <div className="flex justify-between items-center font-bold">
                <span>Total Tax Collected</span>
                <span className="text-blue-600">$4,570</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
