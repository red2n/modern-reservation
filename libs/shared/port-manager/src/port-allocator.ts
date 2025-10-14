import { PortRegistry } from './port-registry';
import { PortValidator } from './port-validator';
import { ServiceCategory, PortAllocation } from './types';
import * as fs from 'fs';
import * as path from 'path';

export class PortAllocator {
  private static allocations: Map<string, PortAllocation> = new Map();
  private static readonly ALLOCATION_FILE = path.join(process.cwd(), '.port-allocations.json');

  static loadAllocations(): void {
    if (fs.existsSync(this.ALLOCATION_FILE)) {
      try {
        const data = fs.readFileSync(this.ALLOCATION_FILE, 'utf-8');
        const allocations = JSON.parse(data);
        this.allocations = new Map(Object.entries(allocations));
      } catch (error) {
        console.error('Failed to load port allocations:', error);
      }
    }
  }

  static saveAllocations(): void {
    try {
      const data = Object.fromEntries(this.allocations);
      fs.writeFileSync(this.ALLOCATION_FILE, JSON.stringify(data, null, 2));
    } catch (error) {
      console.error('Failed to save port allocations:', error);
    }
  }

  static allocatePort(serviceName: string, category: ServiceCategory): number | null {
    const registered = PortRegistry.getServiceByName(serviceName);
    if (registered) {
      return registered.port;
    }

    const existing = this.allocations.get(serviceName);
    if (existing && !PortValidator.isPortInUse(existing.port)) {
      return existing.port;
    }

    const availablePorts = PortRegistry.getAvailablePortsInCategory(category);
    for (const port of availablePorts) {
      if (!PortValidator.isPortInUse(port)) {
        const allocation: PortAllocation = {
          service: serviceName,
          port,
          timestamp: new Date(),
          pid: process.pid,
        };
        this.allocations.set(serviceName, allocation);
        this.saveAllocations();
        return port;
      }
    }

    return null;
  }

  static releasePort(serviceName: string): void {
    this.allocations.delete(serviceName);
    this.saveAllocations();
  }

  static getAllocations(): Map<string, PortAllocation> {
    return new Map(this.allocations);
  }
}
