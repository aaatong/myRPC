package org.example.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ZKRegistration {

    private static final ZKRegistration instance = new ZKRegistration();

    private final CuratorFramework client;

    private ServiceInstanceBuilder<ServiceDetail> sib;

    private ServiceDiscovery<ServiceDetail> serviceDiscovery;

    // todo: the number of ServiceInstance may be too large
    private final Map<String, ServiceInstance<ServiceDetail>> serviceMap = new HashMap<>();

    private ZKRegistration() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        try {
            client.blockUntilConnected();
            sib = ServiceInstance.builder();
            serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDetail.class)
                    .client(client)
                    .serializer(new JsonInstanceSerializer<ServiceDetail>(ServiceDetail.class))
                    .basePath(ServiceDetail.REGISTER_ROOT_PATH)
                    .build();
            serviceDiscovery.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ZKRegistration getInstance() {
        return instance;
    }

    public void registerService(String service, String host, int port) {
        String serviceID = service + "/" + host + ":" + port;

        if (serviceMap.containsKey(serviceID)) {
            System.out.println("Service exists!");
            return;
        }

        sib.address(host)
                .port(port)
                .name(service)
                .payload(new ServiceDetail(service))
                .serviceType(ServiceType.PERMANENT);

        ServiceInstance<ServiceDetail> instance = sib.build();
        try {
            serviceDiscovery.registerService(instance);
            serviceMap.put(serviceID, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unRegisterService(String service, String host, int port) {
        String serviceID = service + "/" + host + ":" + port;

        if (!serviceMap.containsKey(serviceID)) {
            System.out.println("Service doesn't exist!");
            return;
        }
        try {
            serviceDiscovery.unregisterService(serviceMap.get(serviceID));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            serviceDiscovery.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.close();
    }
}
