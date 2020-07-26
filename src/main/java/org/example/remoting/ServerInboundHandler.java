package org.example.remoting;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.proxy.provider.ServiceInvoker;
import org.example.rpc.protocol.RPCRequest;

public class ServerInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New connection coming!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Request received!");
        RPCRequest request = (RPCRequest) msg;
        ServiceInvoker.invoke(request, ctx.channel());
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // todo: exception handling
        // cause.printStackTrace();
        ctx.close();
    }
}
