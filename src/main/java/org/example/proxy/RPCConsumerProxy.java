package org.example.proxy;

import org.example.rpc.protocol.RPCRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RPCConsumerProxy implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest request = new RPCRequest();
        request.setInterfaceName(method.getDeclaringClass().getCanonicalName());
        request.setMethodName(method.getName());
        request.setArgs(args);
        // todo: send this request to remote provider, get response, and return result
        return null;
    }
}
