package org.example.rpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serializer {
    private static KryoSerializer instance = new KryoSerializer();

    private static ThreadLocal<Kryo> threadKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo initKryo = new Kryo();
            initKryo.register(RPCRequest.class);
            initKryo.register(RPCResponse.class);
            return initKryo;
        }
    };

    private KryoSerializer(){}

    public static KryoSerializer getInstance() {
        return instance;
    }

    @Override
    public byte[] serialize(Object object) {
        Kryo kryo = threadKryo.get();
        Output output = new Output(64);
        kryo.writeObject(output, object);
        return output.toBytes();
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = threadKryo.get();
        Input input = new Input(bytes);
        return kryo.readObject(input, clazz);
    }
}
