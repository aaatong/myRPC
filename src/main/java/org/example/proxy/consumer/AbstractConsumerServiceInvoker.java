package org.example.proxy.consumer;

import org.example.remoting.TransportClient;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractConsumerServiceInvoker implements ConsumerServiceInvoker {

    TransportClient tranport = new TransportClient();

    public AbstractConsumerServiceInvoker() {
    }

    public abstract Object invoke(RPCRequest request);
}
