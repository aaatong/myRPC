package org.example.proxy;

import io.netty.channel.Channel;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceInvoker {
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void invoke(RPCRequest request, Channel channel) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String interfaceName = request.getInterfaceName();
                    Object service = ServiceProvider.getService(interfaceName);
                    Method method = service.getClass().getMethod(request.getMethodName(), request.getArgTypes());
                    Object result = method.invoke(service, request.getArgs());
                    RPCResponse response = new RPCResponse();
                    response.setRoundID(request.getRoundID());
                    response.setResult(result);
                    channel.writeAndFlush(response);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
