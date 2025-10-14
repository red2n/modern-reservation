/**
 * Guest Portal Component
 * Single Responsibility: Public-facing booking interface for guests
 * Based on PRD Guest User persona requirements
 */

'use client';

import React from 'react';
import { DashboardLayout, type ModuleId } from '@/components/DashboardLayout';

export function GuestPortal() {
  // Limited modules available to guests
  const availableModules: ModuleId[] = [
    'RESERVATIONS', // View own reservations only
    'AVAILABILITY', // View available rooms
    'DOCUMENTATION', // Help and guides
  ];

  return (
    <DashboardLayout
      title="Guest Portal"
      subtitle="Book your perfect stay"
      availableModules={availableModules}
    >
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg shadow-lg text-white p-8">
          <h2 className="text-3xl font-bold mb-4">Welcome to Modern Reservation</h2>
          <p className="text-blue-100 text-lg mb-6">
            Experience luxury accommodation with our world-class hospitality service.
          </p>
          <button className="bg-white text-blue-600 px-6 py-3 rounded-lg font-medium hover:bg-gray-50 transition-colors">
            Book Now
          </button>
        </div>

        {/* Quick Booking Form */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-semibold text-gray-900 mb-6">Quick Booking</h3>
          <form className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Check-in Date</label>
              <input
                type="date"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Check-out Date</label>
              <input
                type="date"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Guests</label>
              <select className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="1">1 Guest</option>
                <option value="2">2 Guests</option>
                <option value="3">3 Guests</option>
                <option value="4">4+ Guests</option>
              </select>
            </div>
            <div className="flex items-end">
              <button
                type="submit"
                className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
              >
                Search Rooms
              </button>
            </div>
          </form>
        </div>

        {/* Featured Room Types */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-semibold text-gray-900 mb-6">Our Room Types</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="border rounded-lg overflow-hidden">
              <div className="h-48 bg-gray-200"></div>
              <div className="p-4">
                <h4 className="font-semibold text-lg text-gray-900">Standard Room</h4>
                <p className="text-gray-600 mt-2">
                  Comfortable accommodation with modern amenities
                </p>
                <div className="mt-4 flex justify-between items-center">
                  <span className="text-2xl font-bold text-blue-600">$120/night</span>
                  <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                    Book Now
                  </button>
                </div>
              </div>
            </div>

            <div className="border rounded-lg overflow-hidden">
              <div className="h-48 bg-gray-200"></div>
              <div className="p-4">
                <h4 className="font-semibold text-lg text-gray-900">Deluxe Suite</h4>
                <p className="text-gray-600 mt-2">Spacious suite with premium facilities</p>
                <div className="mt-4 flex justify-between items-center">
                  <span className="text-2xl font-bold text-blue-600">$200/night</span>
                  <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                    Book Now
                  </button>
                </div>
              </div>
            </div>

            <div className="border rounded-lg overflow-hidden">
              <div className="h-48 bg-gray-200"></div>
              <div className="p-4">
                <h4 className="font-semibold text-lg text-gray-900">Presidential Suite</h4>
                <p className="text-gray-600 mt-2">Luxury suite with exclusive services</p>
                <div className="mt-4 flex justify-between items-center">
                  <span className="text-2xl font-bold text-blue-600">$500/night</span>
                  <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                    Book Now
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Services & Amenities */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-semibold text-gray-900 mb-6">Services & Amenities</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="w-12 h-12 bg-blue-100 rounded-full mx-auto mb-3 flex items-center justify-center">
                <span className="text-blue-600 text-xl">🏊</span>
              </div>
              <h4 className="font-medium text-gray-900">Swimming Pool</h4>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-blue-100 rounded-full mx-auto mb-3 flex items-center justify-center">
                <span className="text-blue-600 text-xl">💪</span>
              </div>
              <h4 className="font-medium text-gray-900">Fitness Center</h4>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-blue-100 rounded-full mx-auto mb-3 flex items-center justify-center">
                <span className="text-blue-600 text-xl">🍽️</span>
              </div>
              <h4 className="font-medium text-gray-900">Restaurant</h4>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-blue-100 rounded-full mx-auto mb-3 flex items-center justify-center">
                <span className="text-blue-600 text-xl">🅿️</span>
              </div>
              <h4 className="font-medium text-gray-900">Free Parking</h4>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}
