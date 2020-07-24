package org.example.proxy;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private static Map<String, Object> serviceMap = new HashMap<>();

    public static boolean addService(String serviceName, Object service) {
        if (serviceMap.containsKey(serviceName)) {
            return false;
        }
        serviceMap.put(serviceName, service);
        return true;
    }

    public static Object getService(String serviceName) {
        return serviceMap.getOrDefault(serviceName, null);
    }
}
