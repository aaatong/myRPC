package org.example.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    InetSocketAddress doSelect(List<InetSocketAddress> serviceList) {
        int serviceNum = serviceList.size();
        Random random = new Random();
        int selected = random.nextInt(serviceNum);
        return serviceList.get(selected);
    }
}
