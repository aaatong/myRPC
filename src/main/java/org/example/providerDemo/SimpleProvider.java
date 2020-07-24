package org.example.providerDemo;

import org.example.proxy.ServiceProvider;
import org.example.remoting.TransportServer;
import org.example.services.Addition;

public class SimpleProvider {
    public static void main(String[] args) {
        TransportServer server = new TransportServer();
        ServiceProvider.addService(Addition.class.getCanonicalName(), new AdditionImpl());
        try {
            server.start();
        } finally {
            server.close();
        }
    }
}
