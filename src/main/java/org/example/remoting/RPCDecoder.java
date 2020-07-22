package org.example.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.rpc.protocol.RPCResponse;
import org.example.rpc.serialize.KryoSerializer;
import org.example.rpc.serialize.Serializer;

import java.util.List;

public class RPCDecoder extends ByteToMessageDecoder {

    private Class<?> targetClass;

    public RPCDecoder(Class<?> clazz) {
        this.targetClass = clazz;
    }

    private Serializer serializer = KryoSerializer.getInstance();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= 4) {
            int index = in.readerIndex();
            int msgLength = in.readInt();
            if (msgLength <= 0) {
                return;
            }
            if (in.readableBytes() >= msgLength) {
                byte[] msgBytes = new byte[msgLength];
                in.readBytes(msgBytes);
                Object response = serializer.deSerialize(msgBytes, targetClass);
                out.add(response);
            } else {
                in.readerIndex(index);
            }
        }
    }
}
