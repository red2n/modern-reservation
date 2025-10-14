import { Eureka } from 'eureka-js-client';

export async function registerWithEureka(port: number): Promise<void> {
  const client = new Eureka({
    instance: {
      app: 'AUTH-SERVICE',
      hostName: 'localhost',
      ipAddr: '127.0.0.1',
      statusPageUrl: `http://localhost:${port}/health`,
      healthCheckUrl: `http://localhost:${port}/health`,
      port: {
        $: port,
        '@enabled': true,
      },
      vipAddress: 'auth-service',
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn',
      },
    },
    eureka: {
      host: process.env.EUREKA_HOST || 'localhost',
      port: Number(process.env.EUREKA_PORT) || 8761,
      servicePath: '/eureka/apps/',
      maxRetries: 10,
      requestRetryDelay: 2000,
    },
  });

  return new Promise((resolve, reject) => {
    client.start((error) => {
      if (error) {
        console.error('Failed to register with Eureka:', error);
        reject(error);
      } else {
        console.log('✅ Registered with Eureka successfully');
        resolve();
      }
    });
  });
}
