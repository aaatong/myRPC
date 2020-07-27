package org.example.proxy.consumer;

import org.example.remoting.TransportClient;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RPCConsumerProxy implements InvocationHandler {

    TransportClient tranport;

    private final ConsumerServiceInvoker serviceInvoker = new CircuitBreakerInvoker();

    public RPCConsumerProxy() {
        tranport = new TransportClient();
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest request = new RPCRequest();
        request.setRoundID(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getCanonicalName());
        request.setMethodName(method.getName());
        request.setArgs(args);
        request.setArgTypes(method.getParameterTypes());

        return serviceInvoker.invoke(request);
    }
}
