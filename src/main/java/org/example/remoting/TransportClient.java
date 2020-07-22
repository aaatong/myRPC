package org.example.remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TransportClient {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;

    private Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    public TransportClient() {
        group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RPCEncoder())
                                .addLast(new RPCDecoder(RPCResponse.class))
                                .addLast(new ClientInboundHandler());
                    }
                });
    }

    public Channel doConnect(InetSocketAddress address) {
        try {
            ChannelFuture f = bootstrap.connect(address).sync();
            return f.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CompletableFuture<RPCResponse> sendRequest(RPCRequest request, InetSocketAddress address) {
        String key = address.toString();
        Channel targetChannel = channelMap.get(key);
        if (targetChannel == null || !targetChannel.isActive()) {
            targetChannel = doConnect(address);
            channelMap.put(address.toString(), targetChannel);
        }
        CompletableFuture<RPCResponse> responseFuture = new CompletableFuture<>();
        PendingRequests.put(request.getRoundID(), responseFuture);
        targetChannel.writeAndFlush(request).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("Client send a message!");
            } else {

            }
        });
        return responseFuture;
    }

    public void close() {
        group.shutdownGracefully();
    }
}
