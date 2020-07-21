package org.example.consumerDemo;

import org.example.proxy.RPCConsumerProxy;
import org.example.services.Addition;

public class SimpleConsumer {
    public static void main(String[] args) {
        RPCConsumerProxy consumerProxy = new RPCConsumerProxy();
        Addition additionService = consumerProxy.getService(Addition.class);
        int result = additionService.add(28, 22);
        System.out.println(result);
    }
}
