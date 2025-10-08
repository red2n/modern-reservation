import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  // Transpile linked packages from libs/shared
  transpilePackages: ['@modern-reservation/graphql-client', '@modern-reservation/schemas'],
};

export default nextConfig;
