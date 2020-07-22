package org.example.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.serialize.KryoSerializer;
import org.example.rpc.serialize.Serializer;

public class RPCEncoder extends MessageToByteEncoder<Object> {

    public RPCEncoder() {

    }

    private final Serializer serializer = KryoSerializer.getInstance();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] msgBytes = serializer.serialize(msg);
        out.writeInt(msgBytes.length);
        out.writeBytes(msgBytes);
    }
}
