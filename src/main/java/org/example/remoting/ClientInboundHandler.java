package org.example.remoting;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.example.rpc.protocol.RPCResponse;

import java.util.concurrent.CompletableFuture;

public class ClientInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RPCResponse response = (RPCResponse)msg;
        String roundID = response.getRoundID();
        PendingRequests.complete(roundID, response);
    }
}
