export interface ServicePort {
  name: string;
  port: number;
  protocol: 'tcp' | 'udp' | 'http' | 'https' | 'grpc';
  description: string;
  category: ServiceCategory;
  environment?: 'development' | 'staging' | 'production' | 'all';
  internal?: boolean; // Internal-only port (not exposed externally)
}

export enum ServiceCategory {
  // Infrastructure
  DATABASE = 'DATABASE',
  CACHE = 'CACHE',
  MESSAGE_QUEUE = 'MESSAGE_QUEUE',
  SERVICE_DISCOVERY = 'SERVICE_DISCOVERY',
  CONFIG = 'CONFIG',
  GATEWAY = 'GATEWAY',

  // Business Services - Java
  JAVA_BUSINESS = 'JAVA_BUSINESS',

  // Business Services - Node.js
  NODE_SERVICE = 'NODE_SERVICE',

  // Frontend
  FRONTEND = 'FRONTEND',

  // Monitoring & Observability
  MONITORING = 'MONITORING',
  TRACING = 'TRACING',
  LOGGING = 'LOGGING',

  // Development Tools
  DEV_TOOLS = 'DEV_TOOLS',
}

export interface PortRange {
  start: number;
  end: number;
  category: ServiceCategory;
  description: string;
}

export interface PortAllocation {
  service: string;
  port: number;
  timestamp: Date;
  pid?: number;
}
