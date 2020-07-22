package org.example.remoting;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.rpc.protocol.RPCRequest;

public class TransportServer {
    private EventLoopGroup group;
    private ServerBootstrap bootstrap;

    public TransportServer() {
        group = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RPCDecoder(RPCRequest.class))
                                .addLast(new ServerInboundHandler())
                                .addLast(new RPCEncoder());
                    }
                });
    }

    public void start() {
        try {
            bootstrap.bind(12306).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        group.shutdownGracefully();
    }
}
