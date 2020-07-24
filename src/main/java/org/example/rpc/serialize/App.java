package org.example.rpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.example.rpc.protocol.RPCRequest;

import java.io.FileNotFoundException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws FileNotFoundException {
        Serializer serializer = KryoSerializer.getInstance();

        Person jack = new Person("Jack", 20);
        RPCRequest request = new RPCRequest();

        byte[] jackBytes = serializer.serialize(request);


        RPCRequest who = serializer.deSerialize(jackBytes, RPCRequest.class);
        System.out.println(who);
    }
}

class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    Person() {

    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
