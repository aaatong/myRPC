package org.example.proxy;

import org.example.loadbalance.LoadBalance;
import org.example.loadbalance.RandomLoadBalance;
import org.example.registry.ZKServiceDiscovery;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo: singleton factory
public class ServiceProviderManager {

    private static final Map<String, List<InetSocketAddress>> providerMap = new HashMap<>();

    private static final ZKServiceDiscovery serviceDiscovery = ZKServiceDiscovery.getInstance();

    private static final LoadBalance loadBalance = new RandomLoadBalance();

    public static InetSocketAddress getServiceProvider(String service) {
        List<InetSocketAddress> providerList;
        if (providerMap.containsKey(service)) {
            providerList = providerMap.get(service);
        } else {
            providerList = serviceDiscovery.lookupService(service);
            providerMap.put(service, providerList);
        }

        return loadBalance.select(providerList);
    }
}
