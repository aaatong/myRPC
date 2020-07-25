package org.example.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZKServiceDiscovery {
    private static final ZKServiceDiscovery instance = new ZKServiceDiscovery();

    private final CuratorFramework client;

    private ServiceDiscovery<ServiceDetail> serviceDiscovery;

    private ZKServiceDiscovery() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        try {
            client.blockUntilConnected();
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

    public static ZKServiceDiscovery getInstance() {
        return instance;
    }

    public List<InetSocketAddress> lookupService(String service) {
        try {
            Collection<ServiceInstance<ServiceDetail>> services = serviceDiscovery.queryForInstances(service);
            List<InetSocketAddress> serviceList = new ArrayList<>();
            for (ServiceInstance<ServiceDetail> serviceInstance : services) {
                serviceList.add(new InetSocketAddress(serviceInstance.getAddress(), serviceInstance.getPort()));
            }
            return serviceList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
