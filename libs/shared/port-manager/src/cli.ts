#!/usr/bin/env node

import { PortRegistry } from './port-registry';
import { PortValidator } from './port-validator';
import { PortAllocator } from './port-allocator';
import { ConfigGenerator } from './config-generator';
import { ServiceCategory } from './types';

const command = process.argv[2];
const args = process.argv.slice(3);

function printHelp() {
  console.log(`
Modern Reservation Port Manager

Usage:
  port-manager <command> [options]

Commands:
  list                    List all registered services and ports
  check                   Check for port conflicts and usage
  report                  Generate detailed port report
  security                Generate security report
  env                     Export as environment variables
  docker-env              Export as Docker Compose environment file
  find <category>         Find available port in category
  allocate <name> <cat>   Allocate dynamic port for service
  validate                Validate all port configurations

  Config Generation:
  config-master           Generate master .env file
  config-spring <service> Generate Spring Boot application.yml
  config-node <service>   Generate Node.js .env file
  config-ts               Generate TypeScript constants
  config-java             Generate Java constants class
  config-k8s              Generate Kubernetes ConfigMap

Examples:
  port-manager list
  port-manager check
  port-manager security
  port-manager config-master > .env.ports
  port-manager config-spring reservation-engine > application.yml
  port-manager config-node auth-service > .env
`);
}

async function main() {
  switch (command) {
    case 'list': {
      const services = PortRegistry.getAllServices();
      console.log('Registered Services:');
      console.log('===================');
      services.forEach((service) => {
        const security = service.internal ? '🔒' : '🌐';
        console.log(
          `${security} ${service.name.padEnd(30)} ${String(service.port).padEnd(6)} ${service.description}`
        );
      });
      break;
    }

    case 'check': {
      const validation = PortValidator.validateAllPorts();
      console.log('Port Status Check:');
      console.log('==================');
      console.log(`Conflicts: ${validation.conflicts.length}`);
      console.log(`In Use: ${validation.inUse.length}`);

      if (validation.conflicts.length > 0) {
        console.log('\n⚠️ Conflicts:');
        validation.conflicts.forEach((s) => console.log(`  - ${s.name}: ${s.port}`));
      }

      if (validation.inUse.length > 0) {
        console.log('\n✅ Currently In Use:');
        validation.inUse.forEach((s) => console.log(`  - ${s.name}: ${s.port}`));
      }
      break;
    }

    case 'report': {
      const report = PortValidator.getPortReport();
      console.log(report);
      break;
    }

    case 'security': {
      const report = PortRegistry.getSecurityReport();
      console.log(report);
      break;
    }

    case 'env': {
      const env = PortRegistry.exportAsEnvironmentVariables();
      Object.entries(env).forEach(([key, value]) => {
        console.log(`export ${key}=${value}`);
      });
      break;
    }

    case 'docker-env': {
      const dockerEnv = PortRegistry.exportAsDockerComposeEnvironment();
      console.log(dockerEnv);
      break;
    }

    case 'find': {
      const category = args[0] as ServiceCategory;
      if (!category) {
        console.error('Please specify a category');
        process.exit(1);
      }

      const available = PortRegistry.getAvailablePortsInCategory(category);
      console.log(`Available ports in ${category}:`);
      console.log(available.slice(0, 10).join(', '));
      break;
    }

    case 'allocate': {
      const [name, category] = args;
      if (!name || !category) {
        console.error('Usage: port-manager allocate <name> <category>');
        process.exit(1);
      }

      PortAllocator.loadAllocations();
      const port = PortAllocator.allocatePort(name, category as ServiceCategory);
      if (port) {
        console.log(`Allocated port ${port} for ${name}`);
      } else {
        console.error(`Failed to allocate port for ${name}`);
      }
      break;
    }

    case 'validate': {
      const services = PortRegistry.getAllServices();
      let hasErrors = false;

      const portMap = new Map<number, string[]>();
      services.forEach((service) => {
        if (!portMap.has(service.port)) {
          portMap.set(service.port, []);
        }
        portMap.get(service.port)!.push(service.name);
      });

      portMap.forEach((services, port) => {
        if (services.length > 1) {
          console.error(`❌ Port conflict on ${port}: ${services.join(', ')}`);
          hasErrors = true;
        }
      });

      if (!hasErrors) {
        console.log('✅ All port configurations are valid');
      }
      break;
    }

    case 'config-master': {
      const config = ConfigGenerator.generateMasterEnv();
      console.log(config);
      break;
    }

    case 'config-spring': {
      const serviceName = args[0];
      if (!serviceName) {
        console.error('Please specify a service name');
        console.error('Usage: port-manager config-spring <service-name>');
        process.exit(1);
      }
      const config = ConfigGenerator.generateSpringBootConfig(serviceName);
      console.log(config);
      break;
    }

    case 'config-node': {
      const serviceName = args[0];
      if (!serviceName) {
        console.error('Please specify a service name');
        console.error('Usage: port-manager config-node <service-name>');
        process.exit(1);
      }
      const config = ConfigGenerator.generateNodeEnvConfig(serviceName);
      console.log(config);
      break;
    }

    case 'config-ts': {
      const config = ConfigGenerator.generateTypeScriptConfig();
      console.log(config);
      break;
    }

    case 'config-java': {
      const config = ConfigGenerator.generateJavaConfig();
      console.log(config);
      break;
    }

    case 'config-k8s': {
      const config = ConfigGenerator.generateK8sConfigMap();
      console.log(config);
      break;
    }

    default:
      printHelp();
  }
}

main().catch(console.error);
