package org.example.consumerDemo;

import org.example.proxy.consumer.RPCConsumerProxy;
import org.example.services.Addition;

public class SimpleConsumer {
    public static void main(String[] args) {
        RPCConsumerProxy consumerProxy = new RPCConsumerProxy();
        Addition additionService = consumerProxy.getService(Addition.class);
        int result = additionService.add(28, 21);
        System.out.println("RPC result: " + result);
    }
}
