package org.example.consumerDemo;

import org.example.proxy.consumer.RPCConsumerProxy;
import org.example.services.Addition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SimpleConsumer {
    public static void main(String[] args) throws InterruptedException {
        RPCConsumerProxy consumerProxy = new RPCConsumerProxy();
        Addition additionService = consumerProxy.getService(Addition.class);

        CompletableFuture<Integer> result = additionService.addAsync(28, 21);
        try {
            System.out.println("RPC result: " + result.get(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            // e.printStackTrace();
            // todo: timeout handling
            System.out.println("Exception!");
        }

    }
}
