package org.example.proxy.consumer;

import org.example.rpc.protocol.RPCRequest;

public interface ConsumerServiceInvoker {

    Object invoke(RPCRequest request);

}
