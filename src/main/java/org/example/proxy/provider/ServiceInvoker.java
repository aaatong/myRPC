package org.example.proxy.provider;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.FutureListener;
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
                    // todo: populate response code according to exception types
                    String interfaceName = request.getInterfaceName();
                    Object service = ServiceManager.getService(interfaceName);
                    Method method = service.getClass().getMethod(request.getMethodName(), request.getArgTypes());
                    Object result = method.invoke(service, request.getArgs());
                    RPCResponse response = new RPCResponse();
                    response.setRoundID(request.getRoundID());
                    response.setResult(result);
                    response.setCode(RPCResponse.ResponseCode.SUCCESS);

                    channel.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                    });
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
