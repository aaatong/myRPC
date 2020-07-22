package org.example.rpc.serialize;

public interface Serializer {
    byte[] serialize(Object object);

    <T> T deSerialize(byte[] bytes, Class<T> clazz);
}
