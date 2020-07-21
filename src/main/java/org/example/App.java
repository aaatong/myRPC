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

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(Person.class);

        Person jack = new Person("Jack", 20);
        ByteBuf buffer = Unpooled.buffer(1024);

        Output output = new Output(new ByteBufOutputStream(buffer));
        kryo.writeObject(output, jack);
        output.close();

        Input input = new Input(new ByteBufInputStream(buffer));
        Person who = kryo.readObject(input, Person.class);
        System.out.println(who);
        input.close();
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
