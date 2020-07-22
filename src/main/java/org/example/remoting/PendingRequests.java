package org.example.remoting;

import io.netty.channel.ChannelPromise;
import org.example.rpc.protocol.RPCResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PendingRequests {
    private static final Map<String, CompletableFuture<RPCResponse>> pendingRequests = new HashMap<>();

    public static void put(String id, CompletableFuture<RPCResponse> responseFuture) {
        pendingRequests.put(id, responseFuture);
    }

    public static void complete(String id, RPCResponse response) {
        CompletableFuture<RPCResponse> responseFuture = pendingRequests.get(id);
        responseFuture.complete(response);
    }
}
