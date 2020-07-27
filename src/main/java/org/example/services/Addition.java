package org.example.services;

import java.util.concurrent.CompletableFuture;

public interface Addition {

    int add(int a, int b);

    default CompletableFuture<Integer> addAsync(int a, int b) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        result.complete(add(a, b));
        return result;
    }
}
