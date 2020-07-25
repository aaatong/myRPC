package org.example.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public InetSocketAddress select(List<InetSocketAddress> serviceList) {
        if (serviceList == null || serviceList.size() == 0) {
            return null;
        }
        if (serviceList.size() == 1) {
            return serviceList.get(0);
        }
        return doSelect(serviceList);
    }

    abstract InetSocketAddress doSelect(List<InetSocketAddress> serviceList);
}
