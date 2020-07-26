package org.example.proxy.provider;

import org.example.registry.ZKRegistration;

import java.util.HashMap;
import java.util.Map;

public class ServiceManager {
    private static Map<String, Object> serviceMap = new HashMap<>();

    private static ZKRegistration zkRegistration = ZKRegistration.getInstance();

    public static boolean addService(String serviceName, Object service) {
        if (serviceMap.containsKey(serviceName)) {
            return false;
        }
        serviceMap.put(serviceName, service);
        zkRegistration.registerService(serviceName, "localhost", 12306);
        return true;
    }

    public static Object getService(String serviceName) {
        return serviceMap.getOrDefault(serviceName, null);
    }
}
