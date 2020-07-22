package org.example;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(Person.class);

        Person jack = new Person("Jack", 20);

        Output output = new Output(64);
        kryo.writeObject(output, jack);
        byte[] jackBytes = output.toBytes();
        System.out.println(jackBytes.length);

        Input input = new Input(jackBytes);
        Person who = kryo.readObject(input, Person.class);
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
