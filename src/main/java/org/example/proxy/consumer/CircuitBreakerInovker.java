package org.example.proxy.consumer;

import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreakerInovker extends AbstractConsumerServiceInvoker{

    private ScheduledExecutorService statsService = Executors.newSingleThreadScheduledExecutor();

    private Map<String, Deque<Bucket>> serviceStats = new HashMap<>();

    private Map<String, CircuitState> serviceState = new HashMap<>();

    private static final int REQUEST_VOLUME_THRESHOLD = 10;

    private static final double FAILURE_PERCENTAGE_THRESHOLD = 0.8;

    private static final int BUCKET_NUMBER = 10;

    CircuitBreakerInovker() {
        statsService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Deque<Bucket>> entry : serviceStats.entrySet()) {
                    int total = 0;
                    int success = 0;
                    Deque<Bucket> slideWindow = entry.getValue();
                    for (Bucket b : slideWindow) {
                        total += b.getTotal();
                        success += b.getSuccess();
                    }
                    double failureRate = 1.0 - success / (double) total;
                    if (total > REQUEST_VOLUME_THRESHOLD && failureRate < FAILURE_PERCENTAGE_THRESHOLD) {
                        // todo: circuit break

                    }
                    slideWindow.addLast(slideWindow.removeFirst().reset());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private class Bucket {
        // total number of invocation
        private int total;
        // the number of successful invocation
        private int success;

        private int timeout;

        private int failure;

        public int getTotal() {
            return total;
        }

        public int getSuccess() {
            return success;
        }

        public int getTimeout() {
            return timeout;
        }

        public int getFailure() {
            return failure;
        }

        public void incrementSuccess() {
            success += 1;
            total += 1;
        }

        public void incrementFailure() {
            failure += 1;
            total += 1;
        }

        public void incrementTimeout() {
            timeout += 1;
            total += 1;
        }

        public Bucket reset() {
            total = success = failure = timeout = 0;
            return this;
        }

    }

    private enum CircuitState {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private Bucket getCurrentBucket(String serviceInstance) {
        if (!serviceStats.containsKey(serviceInstance)) {
            Deque<Bucket> newSlideWindow = new LinkedList<>();
            for (int i = 0; i < BUCKET_NUMBER; i++) {
                newSlideWindow.add(new Bucket());
            }
            serviceStats.put(serviceInstance, newSlideWindow);
            return newSlideWindow.getLast();
        }
        Deque<Bucket> slideWindow = serviceStats.get(serviceInstance);
        return slideWindow.getLast();
    }

    public void incrementSuccess(String serviceInstance) {
        Bucket b = getCurrentBucket(serviceInstance);
        b.incrementSuccess();
    }

    public void incrementTimeout(String serviceInstance) {
        Bucket b = getCurrentBucket(serviceInstance);
        b.incrementTimeout();
    }

    public void incrementFailure(String serviceInstance) {
        Bucket b = getCurrentBucket(serviceInstance);
        b.incrementFailure();
    }

    @Override
    public Object invoke(RPCRequest request) {
        InetSocketAddress providerAddr = ServiceProviderManager.getServiceProvider(request.getInterfaceName());
        String serviceInstance = request.getInterfaceName() + providerAddr.toString();

        CompletableFuture<RPCResponse> responseFuture = tranport.sendRequest(request, providerAddr);
        responseFuture.whenCompleteAsync((response, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof TimeoutException) {
                    incrementTimeout(serviceInstance);
                }
            } else {
                if (response.getCode() != RPCResponse.ResponseCode.SUCCESS) {
                    incrementFailure(serviceInstance);
                } else {
                    incrementSuccess(serviceInstance);
                }
            }
        });
        try {
            return responseFuture.get(5, TimeUnit.SECONDS).getResult();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            if (e instanceof TimeoutException) {
                responseFuture.completeExceptionally(e);
            }
            // todo: fallback
            return null;
        }
    }
}
