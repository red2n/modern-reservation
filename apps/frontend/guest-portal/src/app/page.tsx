'use client';

import React from 'react';
import { RoleBasedRouter } from '@/components/RoleBasedRouter';
import { AuthProvider } from '@/contexts/AuthContext';

export default function Home() {
  return (
    <AuthProvider>
      <RoleBasedRouter />
    </AuthProvider>
  );
}
