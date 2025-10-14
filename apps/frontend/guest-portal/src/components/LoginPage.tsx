/**
 * Login Page Component
 * Single Responsibility: User authentication interface
 * Integrates with AuthContext for role-based access
 */

'use client';

import type React from 'react';
import { useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';

export function LoginPage() {
  const { login, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      await login(email, password);
    } catch (err) {
      setError('Invalid credentials. Please try again.');
    }
  };

  // Demo accounts for different roles
  const demoAccounts = [
    { role: 'Front Desk Staff', email: 'frontdesk@hotel.com', password: 'demo123' },
    { role: 'Reservation Manager', email: 'reservations@hotel.com', password: 'demo123' },
    { role: 'Hotel Administrator', email: 'admin@hotel.com', password: 'demo123' },
    { role: 'Finance Team', email: 'finance@hotel.com', password: 'demo123' },
    { role: 'Housekeeping', email: 'housekeeping@hotel.com', password: 'demo123' },
    { role: 'Property Manager', email: 'manager@hotel.com', password: 'demo123' },
    { role: 'Guest Portal', email: 'guest@example.com', password: 'demo123' },
  ];

  const handleDemoLogin = (demoEmail: string, demoPassword: string) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="max-w-md w-full space-y-8">
        <div className="bg-white rounded-lg shadow-xl p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Modern Reservation</h2>
            <p className="text-gray-600 mt-2">Management System</p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                Email Address
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Enter your email"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Enter your password"
              />
            </div>

            {error && (
              <div className="text-red-600 text-sm text-center p-2 bg-red-50 rounded">{error}</div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Demo Accounts */}
          <div className="mt-8">
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-2">
                <strong>Demo Accounts Available:</strong>
              </p>
              <p className="text-xs text-gray-500 mb-4">
                Click any role below to auto-fill credentials, then click "Sign In"
              </p>
            </div>
            <div className="grid grid-cols-1 gap-2">
              {demoAccounts.map((account) => {
                const isSelected = email === account.email;
                return (
                  <button
                    key={account.email}
                    type="button"
                    onClick={() => handleDemoLogin(account.email, account.password)}
                    className={`text-left p-3 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                      isSelected
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <div className={`font-medium ${isSelected ? 'text-blue-900' : 'text-gray-900'}`}>
                      {account.role}
                    </div>
                    <div className={`${isSelected ? 'text-blue-700' : 'text-gray-600'}`}>
                      {account.email}
                    </div>
                    {isSelected && (
                      <div className="text-xs text-blue-600 mt-1 font-medium">
                        ✓ Credentials loaded - click "Sign In" to continue
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Footer */}
          <div className="mt-6 text-center">
            <p className="text-xs text-gray-500">Modern Reservation Management System v1.0</p>
          </div>
        </div>
      </div>
    </div>
  );
}
