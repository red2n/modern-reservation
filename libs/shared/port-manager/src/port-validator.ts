import { PortRegistry } from './port-registry';
import { ServicePort } from './types';
import { execSync } from 'child_process';

export class PortValidator {
  /**
   * Check if a port is in use on the system
   */
  static isPortInUse(port: number): boolean {
    try {
      const platform = process.platform;

      if (platform === 'linux' || platform === 'darwin') {
        const result = execSync(`lsof -i:${port} 2>/dev/null | grep LISTEN || true`, {
          encoding: 'utf-8',
        });
        return result.trim().length > 0;
      } else if (platform === 'win32') {
        const result = execSync(`netstat -an | findstr :${port}`, { encoding: 'utf-8' });
        return result.includes('LISTENING');
      }

      return false;
    } catch (error) {
      return false;
    }
  }

  /**
   * Validate all registered ports and check for conflicts
   */
  static validateAllPorts(): { conflicts: ServicePort[]; inUse: ServicePort[] } {
    const services = PortRegistry.getAllServices();
    const conflicts: ServicePort[] = [];
    const inUse: ServicePort[] = [];

    // Check for duplicate port assignments
    const portMap = new Map<number, ServicePort[]>();
    services.forEach((service) => {
      if (!portMap.has(service.port)) {
        portMap.set(service.port, []);
      }
      portMap.get(service.port)!.push(service);
    });

    // Find conflicts
    portMap.forEach((services) => {
      if (services.length > 1) {
        conflicts.push(...services);
      }
    });

    // Check which ports are currently in use
    services.forEach((service) => {
      if (this.isPortInUse(service.port)) {
        inUse.push(service);
      }
    });

    return { conflicts, inUse };
  }

  /**
   * Find available port in a specific range
   */
  static findAvailablePort(startPort: number, endPort: number): number | null {
    for (let port = startPort; port <= endPort; port++) {
      if (!PortRegistry.isPortRegistered(port) && !this.isPortInUse(port)) {
        return port;
      }
    }
    return null;
  }

  /**
   * Get a report of all port usage
   */
  static getPortReport(): string {
    const services = PortRegistry.getAllServices();
    const validation = this.validateAllPorts();

    const lines: string[] = [];
    lines.push('='.repeat(80));
    lines.push('PORT MANAGEMENT REPORT - Modern Reservation System');
    lines.push('='.repeat(80));
    lines.push('');

    // Group by category
    const categories = new Set(services.map((s) => s.category));
    categories.forEach((category) => {
      const categoryServices = services.filter((s) => s.category === category);
      lines.push(`[${category}]`);
      lines.push('-'.repeat(40));

      categoryServices.forEach((service) => {
        const status = validation.inUse.includes(service) ? '🟢 IN USE' : '⚪ AVAILABLE';
        const conflict = validation.conflicts.includes(service) ? '⚠️ CONFLICT' : '';
        const security = service.internal ? '🔒 INTERNAL' : '🌐 EXTERNAL';
        lines.push(
          `  ${service.port.toString().padEnd(6)} ${service.name.padEnd(25)} ${status} ${security} ${conflict}`
        );
      });
      lines.push('');
    });

    // Summary
    lines.push('SUMMARY');
    lines.push('-'.repeat(40));
    lines.push(`Total Services: ${services.length}`);
    lines.push(`Ports In Use: ${validation.inUse.length}`);
    lines.push(`Port Conflicts: ${validation.conflicts.length / 2}`);
    lines.push(`External Services: ${PortRegistry.getExternalServices().length}`);
    lines.push(`Internal Services: ${PortRegistry.getInternalServices().length}`);

    if (validation.conflicts.length > 0) {
      lines.push('');
      lines.push('⚠️ PORT CONFLICTS DETECTED:');
      const conflictPorts = new Set(validation.conflicts.map((s) => s.port));
      conflictPorts.forEach((port) => {
        const conflictServices = validation.conflicts.filter((s) => s.port === port);
        lines.push(`  Port ${port}: ${conflictServices.map((s) => s.name).join(', ')}`);
      });
    }

    return lines.join('\n');
  }
}
