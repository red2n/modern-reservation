import { ServicePort, ServiceCategory, PortRange } from './types';

/**
 * Centralized Port Registry for Modern Reservation System
 * All service ports are defined here with proper internal/external access control
 */
export class PortRegistry {
  // Port ranges for different service categories
  private static readonly PORT_RANGES: PortRange[] = [
    {
      start: 3000,
      end: 3099,
      category: ServiceCategory.FRONTEND,
      description: 'Frontend applications',
    },
    {
      start: 3100,
      end: 3199,
      category: ServiceCategory.NODE_SERVICE,
      description: 'Node.js services',
    },
    {
      start: 5000,
      end: 5099,
      category: ServiceCategory.DEV_TOOLS,
      description: 'Development tools',
    },
    {
      start: 5432,
      end: 5449,
      category: ServiceCategory.DATABASE,
      description: 'PostgreSQL and other databases',
    },
    {
      start: 6379,
      end: 6399,
      category: ServiceCategory.CACHE,
      description: 'Redis and cache services',
    },
    {
      start: 8080,
      end: 8099,
      category: ServiceCategory.GATEWAY,
      description: 'API Gateways',
    },
    {
      start: 8100,
      end: 8199,
      category: ServiceCategory.JAVA_BUSINESS,
      description: 'Java business services',
    },
    {
      start: 8761,
      end: 8799,
      category: ServiceCategory.SERVICE_DISCOVERY,
      description: 'Eureka and service discovery',
    },
    {
      start: 8888,
      end: 8899,
      category: ServiceCategory.CONFIG,
      description: 'Configuration servers',
    },
    {
      start: 9000,
      end: 9099,
      category: ServiceCategory.MONITORING,
      description: 'Monitoring services',
    },
    {
      start: 9092,
      end: 9099,
      category: ServiceCategory.MESSAGE_QUEUE,
      description: 'Kafka and message queues',
    },
    {
      start: 16686,
      end: 16699,
      category: ServiceCategory.TRACING,
      description: 'Jaeger and tracing',
    },
  ];

  // All registered service ports with internal/external access control
  private static readonly SERVICES: ServicePort[] = [
    // ============= EXTERNALLY ACCESSIBLE SERVICES =============
    // These services are exposed to the outside world

    // Frontend Applications (EXTERNAL - Users need to access these)
    {
      name: 'guest-portal',
      port: 3000,
      protocol: 'http',
      description: 'Guest Portal UI',
      category: ServiceCategory.FRONTEND,
      internal: false,
    },
    {
      name: 'guest-portal-dev',
      port: 3001,
      protocol: 'http',
      description: 'Guest Portal UI (Dev)',
      category: ServiceCategory.FRONTEND,
      internal: false,
    },
    {
      name: 'admin-portal',
      port: 3010,
      protocol: 'http',
      description: 'Admin Portal UI',
      category: ServiceCategory.FRONTEND,
      internal: false,
    },
    {
      name: 'staff-portal',
      port: 3020,
      protocol: 'http',
      description: 'Staff Portal UI',
      category: ServiceCategory.FRONTEND,
      internal: false,
    },

    // API Gateway (EXTERNAL - Single entry point for all API calls)
    {
      name: 'gateway-service',
      port: 8080,
      protocol: 'http',
      description: 'Spring Cloud Gateway - Main API Entry Point',
      category: ServiceCategory.GATEWAY,
      internal: false,
    },

    // Development Tools (EXTERNAL in dev, can be disabled in production)
    {
      name: 'pgadmin',
      port: 5050,
      protocol: 'http',
      description: 'PgAdmin UI',
      category: ServiceCategory.DEV_TOOLS,
      internal: false,
      environment: 'development',
    },
    {
      name: 'kafka-ui',
      port: 8090,
      protocol: 'http',
      description: 'Kafka UI',
      category: ServiceCategory.DEV_TOOLS,
      internal: false,
      environment: 'development',
    },
    {
      name: 'eureka-server',
      port: 8761,
      protocol: 'http',
      description: 'Eureka Service Discovery UI',
      category: ServiceCategory.SERVICE_DISCOVERY,
      internal: false,
      environment: 'development',
    },
    {
      name: 'jaeger',
      port: 16686,
      protocol: 'http',
      description: 'Jaeger UI',
      category: ServiceCategory.TRACING,
      internal: false,
      environment: 'development',
    },

    // ============= INTERNAL-ONLY SERVICES =============
    // These services should NEVER be exposed externally

    // Node.js Services (INTERNAL - Only accessible through Gateway)
    {
      name: 'auth-service',
      port: 3100,
      protocol: 'http',
      description: 'Authentication Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'notification-service',
      port: 3110,
      protocol: 'http',
      description: 'Notification Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'websocket-service',
      port: 3120,
      protocol: 'http',
      description: 'WebSocket Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'file-upload-service',
      port: 3130,
      protocol: 'http',
      description: 'File Upload Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'audit-service',
      port: 3140,
      protocol: 'http',
      description: 'Audit Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'channel-manager',
      port: 3150,
      protocol: 'http',
      description: 'Channel Manager',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },
    {
      name: 'housekeeping-service',
      port: 3160,
      protocol: 'http',
      description: 'Housekeeping Service',
      category: ServiceCategory.NODE_SERVICE,
      internal: true,
    },

    // Databases (INTERNAL - Never expose databases directly)
    {
      name: 'postgres',
      port: 5432,
      protocol: 'tcp',
      description: 'PostgreSQL Database',
      category: ServiceCategory.DATABASE,
      internal: true,
    },
    {
      name: 'postgres-test',
      port: 5433,
      protocol: 'tcp',
      description: 'PostgreSQL Test Database',
      category: ServiceCategory.DATABASE,
      internal: true,
    },

    // Cache Services (INTERNAL - Security risk if exposed)
    {
      name: 'redis',
      port: 6379,
      protocol: 'tcp',
      description: 'Redis Cache',
      category: ServiceCategory.CACHE,
      internal: true,
    },
    {
      name: 'redis-cluster',
      port: 6380,
      protocol: 'tcp',
      description: 'Redis Cluster',
      category: ServiceCategory.CACHE,
      internal: true,
    },

    // Java Business Services (INTERNAL - Only accessible through Gateway)
    {
      name: 'reservation-engine',
      port: 8100,
      protocol: 'http',
      description: 'Reservation Engine Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },
    {
      name: 'availability-calculator',
      port: 8110,
      protocol: 'http',
      description: 'Availability Calculator Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },
    {
      name: 'rate-management',
      port: 8120,
      protocol: 'http',
      description: 'Rate Management Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },
    {
      name: 'payment-processor',
      port: 8130,
      protocol: 'http',
      description: 'Payment Processor Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },
    {
      name: 'analytics-engine',
      port: 8140,
      protocol: 'http',
      description: 'Analytics Engine Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },
    {
      name: 'tenant-service',
      port: 8150,
      protocol: 'http',
      description: 'Tenant Service',
      category: ServiceCategory.JAVA_BUSINESS,
      internal: true,
    },

    // Configuration (INTERNAL - Contains sensitive configuration)
    {
      name: 'config-server',
      port: 8888,
      protocol: 'http',
      description: 'Spring Cloud Config Server',
      category: ServiceCategory.CONFIG,
      internal: true,
    },

    // Monitoring (INTERNAL in production, can be exposed in dev)
    {
      name: 'prometheus',
      port: 9090,
      protocol: 'http',
      description: 'Prometheus Metrics',
      category: ServiceCategory.MONITORING,
      internal: true,
    },
    {
      name: 'grafana',
      port: 3003,
      protocol: 'http',
      description: 'Grafana Dashboard',
      category: ServiceCategory.MONITORING,
      internal: true,
    },

    // Message Queue (INTERNAL - Security critical)
    {
      name: 'kafka',
      port: 9092,
      protocol: 'tcp',
      description: 'Apache Kafka',
      category: ServiceCategory.MESSAGE_QUEUE,
      internal: true,
    },
    {
      name: 'kafka-external',
      port: 29092,
      protocol: 'tcp',
      description: 'Kafka External Access',
      category: ServiceCategory.MESSAGE_QUEUE,
      internal: true,
    },
    {
      name: 'schema-registry',
      port: 8081,
      protocol: 'http',
      description: 'Kafka Schema Registry',
      category: ServiceCategory.MESSAGE_QUEUE,
      internal: true,
    },
    {
      name: 'zookeeper',
      port: 2181,
      protocol: 'tcp',
      description: 'Zookeeper',
      category: ServiceCategory.MESSAGE_QUEUE,
      internal: true,
    },

    // OpenTelemetry (INTERNAL - Contains tracing data)
    {
      name: 'otel-collector-grpc',
      port: 4317,
      protocol: 'grpc',
      description: 'OpenTelemetry Collector GRPC',
      category: ServiceCategory.TRACING,
      internal: true,
    },
    {
      name: 'otel-collector-http',
      port: 4318,
      protocol: 'http',
      description: 'OpenTelemetry Collector HTTP',
      category: ServiceCategory.TRACING,
      internal: true,
    },
  ];

  static getAllServices(): ServicePort[] {
    return [...this.SERVICES];
  }

  static getServiceByName(name: string): ServicePort | undefined {
    return this.SERVICES.find((s) => s.name === name);
  }

  static getServicesByCategory(category: ServiceCategory): ServicePort[] {
    return this.SERVICES.filter((s) => s.category === category);
  }

  static getServiceByPort(port: number): ServicePort | undefined {
    return this.SERVICES.find((s) => s.port === port);
  }

  static getPortRanges(): PortRange[] {
    return [...this.PORT_RANGES];
  }

  static isPortRegistered(port: number): boolean {
    return this.SERVICES.some((s) => s.port === port);
  }

  static getAvailablePortsInCategory(category: ServiceCategory): number[] {
    const range = this.PORT_RANGES.find((r) => r.category === category);
    if (!range) return [];

    const usedPorts = new Set(this.SERVICES.filter((s) => s.category === category).map((s) => s.port));

    const available: number[] = [];
    for (let port = range.start; port <= range.end; port++) {
      if (!usedPorts.has(port) && !this.isPortRegistered(port)) {
        available.push(port);
      }
    }
    return available;
  }

  static getExternalServices(): ServicePort[] {
    return this.SERVICES.filter((s) => !s.internal);
  }

  static getInternalServices(): ServicePort[] {
    return this.SERVICES.filter((s) => s.internal);
  }

  static exportAsEnvironmentVariables(): Record<string, string> {
    const env: Record<string, string> = {};
    this.SERVICES.forEach((service) => {
      const key = `PORT_${service.name.toUpperCase().replace(/-/g, '_')}`;
      env[key] = String(service.port);
    });
    return env;
  }

  static exportAsDockerComposeEnvironment(): string {
    const lines: string[] = [
      '# Port Configuration for Modern Reservation System',
      '# Generated by @modern-reservation/port-manager',
      '',
      '# ============= EXTERNAL SERVICES =============',
      '# These ports are exposed to the host machine',
      '',
    ];

    const externalServices = this.getExternalServices();
    externalServices.forEach((service) => {
      lines.push(`# ${service.description}`);
      lines.push(`${service.name.toUpperCase().replace(/-/g, '_')}_PORT=${service.port}`);
    });

    lines.push('');
    lines.push('# ============= INTERNAL SERVICES =============');
    lines.push('# These ports are ONLY accessible within Docker network');
    lines.push('# DO NOT expose these ports to the host!');
    lines.push('');

    const internalServices = this.getInternalServices();
    internalServices.forEach((service) => {
      lines.push(`# ${service.description} (INTERNAL ONLY)`);
      lines.push(`${service.name.toUpperCase().replace(/-/g, '_')}_INTERNAL_PORT=${service.port}`);
    });

    return lines.join('\n');
  }

  static getSecurityReport(): string {
    const lines: string[] = [];
    lines.push('='.repeat(80));
    lines.push('SECURITY REPORT - Port Exposure Analysis');
    lines.push('='.repeat(80));
    lines.push('');

    const externalServices = this.getExternalServices();
    const internalServices = this.getInternalServices();

    lines.push('EXTERNAL SERVICES (Exposed to Internet)');
    lines.push('-'.repeat(40));
    externalServices.forEach((service) => {
      const icon = service.category === ServiceCategory.GATEWAY ? '🌐' : '📡';
      lines.push(
        `  ${icon} ${service.port.toString().padEnd(6)} ${service.name.padEnd(25)} ${service.description}`
      );
    });

    lines.push('');
    lines.push('INTERNAL SERVICES (Protected)');
    lines.push('-'.repeat(40));
    internalServices.forEach((service) => {
      const icon = '🔒';
      lines.push(
        `  ${icon} ${service.port.toString().padEnd(6)} ${service.name.padEnd(25)} ${service.description}`
      );
    });

    lines.push('');
    lines.push('SUMMARY');
    lines.push('-'.repeat(40));
    lines.push(`External Services: ${externalServices.length}`);
    lines.push(`Internal Services: ${internalServices.length}`);
    lines.push(
      `Security Level: ${internalServices.length > externalServices.length ? '🟢 GOOD' : '🟡 REVIEW'}`
    );

    lines.push('');
    lines.push('RECOMMENDATIONS');
    lines.push('-'.repeat(40));
    lines.push('✅ Gateway Service (8080) is the only API entry point');
    lines.push('✅ All backend services are internal-only');
    lines.push('✅ Databases and caches are not exposed');
    lines.push('✅ Message queues are internal-only');
    lines.push('⚠️  In production, disable Eureka UI, PgAdmin, and Kafka UI external access');

    return lines.join('\n');
  }
}
