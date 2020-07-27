package org.example.proxy.consumer;

import org.example.rpc.protocol.RPCRequest;
import org.example.rpc.protocol.RPCResponse;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class CircuitBreakerInvoker extends AbstractConsumerServiceInvoker{

    private final Map<String, CircuitBreaker> serviceCircuit = new HashMap<>();

    CircuitBreakerInvoker() {

    }

    private static class CircuitBreaker {

        private static class Bucket {
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

        private static final int REQUEST_VOLUME_THRESHOLD = 10;

        private static final double FAILURE_PERCENTAGE_THRESHOLD = 0.8;

        private static final double PROBE_SUCCESS_PERCENTAGE_THRESHOLD = 0.8;

        private static final int BUCKET_NUMBER = 10;

        private static final int MAX_PROBE_COUNT = 10;

        private final Deque<Bucket> slideWindow;

        private CircuitState state;

        private long stateChangeTime;

        private int probeCount;

        private List<Boolean> probeResult = new ArrayList<>(MAX_PROBE_COUNT);

        private int total;

        private int success;

        private int failure;

        private int timeout;

        private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

        CircuitBreaker() {
            slideWindow = new LinkedList<>();
            for (int i = 0; i < BUCKET_NUMBER; i++) {
                slideWindow.add(new Bucket());
            }

            setState(CircuitState.CLOSED);

            scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    shiftBuckets();
                }
            }, 10, 1, TimeUnit.SECONDS);
        }

        public void shiftBuckets() {
            if (state != CircuitState.CLOSED) {
                return;
            }

            Bucket newestBucket = slideWindow.getLast();
            total += newestBucket.getTotal();
            success += newestBucket.getSuccess();
            failure += newestBucket.getFailure();
            timeout += newestBucket.getTimeout();

            double failureRate = 1.0 - success / (double) total;
            System.out.println("FailureRate: " + failureRate + " total: " + total);
            if (total > REQUEST_VOLUME_THRESHOLD && failureRate > FAILURE_PERCENTAGE_THRESHOLD) {
                // todo: circuit break
                setState(CircuitState.OPEN);
            }
            slideWindow.addLast(slideWindow.removeFirst().reset());
        }

        private void handleProbeResult(boolean result) {
            probeResult.add(result);
            if (probeResult.size() == MAX_PROBE_COUNT) {
                int successProbe = 0;
                for (boolean b : probeResult) {
                    if (b) {
                        successProbe++;
                    }
                }
                if (successProbe / (double)MAX_PROBE_COUNT > PROBE_SUCCESS_PERCENTAGE_THRESHOLD) {
                    setState(CircuitState.CLOSED);
                } else {
                    setState(CircuitState.OPEN);
                }
                probeCount = 0;
                probeResult.clear();
            }
        }

        public void incrementSuccess() {
            if (state == CircuitState.HALF_OPEN) {
                handleProbeResult(true);
                return;
            }
            Bucket newestBucket = slideWindow.getLast();
            newestBucket.incrementSuccess();
        }

        public void incrementFailure() {
            if (state == CircuitState.HALF_OPEN) {
                handleProbeResult(false);
                return;
            }
            Bucket newestBucket = slideWindow.getLast();
            newestBucket.incrementFailure();
        }

        public void incrementTimeout() {
            if (state == CircuitState.HALF_OPEN) {
                handleProbeResult(false);
                return;
            }
            Bucket newestBucket = slideWindow.getLast();
            newestBucket.incrementTimeout();
        }

        public CircuitState getState() {
            return state;
        }

        private void reset() {
            total = success = failure = timeout = 0;
            for (Bucket b : slideWindow) {
                b.reset();
            }
        }

        public void setState(CircuitState state) {
            if (state == CircuitState.CLOSED) {
                reset();
            }
            this.state = state;
            this.stateChangeTime = System.currentTimeMillis();
        }

        public long getStateChangeTime() {
            return stateChangeTime;
        }

        public int getProbeCount() {
            return probeCount;
        }
    }

    private CircuitBreaker getCircuitBreaker(String serviceInstance) {
        if (!serviceCircuit.containsKey(serviceInstance)) {
            serviceCircuit.put(serviceInstance, new CircuitBreaker());
        }
        return serviceCircuit.get(serviceInstance);
    }

    @Override
    public Object invoke(RPCRequest request) {
        // todo: load balance should take failure rate into consideration
        InetSocketAddress providerAddr = ServiceProviderManager.getServiceProvider(request.getInterfaceName());
        String serviceInstance = request.getInterfaceName() + providerAddr.toString();
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceInstance);

        if (circuitBreaker.getState() == CircuitBreaker.CircuitState.CLOSED) {
            return doInvoke(request);
        } else if (circuitBreaker.getState() == CircuitBreaker.CircuitState.HALF_OPEN) {
            return doProbe(request);
        } else {
            if (System.currentTimeMillis() - circuitBreaker.getStateChangeTime() > 5000) {
                circuitBreaker.setState(CircuitBreaker.CircuitState.HALF_OPEN);
                return doProbe(request);
            } else {
                return doFallback();
            }
        }
    }

    private Object doInvoke(RPCRequest request) {
        InetSocketAddress providerAddr = ServiceProviderManager.getServiceProvider(request.getInterfaceName());
        String serviceInstance = request.getInterfaceName() + providerAddr.toString();
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceInstance);

        CompletableFuture<RPCResponse> responseFuture = tranport.sendRequest(request, providerAddr);
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        responseFuture.whenCompleteAsync((response, throwable) -> {
            if (throwable != null) {
                resultFuture.completeExceptionally(throwable);
                if (throwable instanceof TimeoutException) {
                    circuitBreaker.incrementTimeout();
                }
            } else {
                if (response.getCode() != RPCResponse.ResponseCode.SUCCESS) {
                    resultFuture.completeExceptionally(new ClassNotFoundException());
                    circuitBreaker.incrementFailure();
                } else {
                    resultFuture.complete(((CompletableFuture<Object>) (response.getResult())).getNow(null));
                    circuitBreaker.incrementSuccess();
                }
            }
        });
        return resultFuture;
        // try {
        //     return responseFuture.get(5, TimeUnit.SECONDS).getResult();
        // } catch (InterruptedException | ExecutionException | TimeoutException e) {
        //     e.printStackTrace();
        //     if (e instanceof TimeoutException) {
        //         responseFuture.completeExceptionally(e);
        //     }
        //     // todo: fallback
        //     return null;
        // }
    }

    private Object doFallback() {
        // todo: fallback
        CompletableFuture<Object> resultFuture = new CompletableFuture<>();
        resultFuture.complete(123);
        return resultFuture;
    }

    private Object doProbe(RPCRequest request) {
        InetSocketAddress providerAddr = ServiceProviderManager.getServiceProvider(request.getInterfaceName());
        String serviceInstance = request.getInterfaceName() + providerAddr.toString();
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceInstance);

        if (circuitBreaker.getProbeCount() < CircuitBreaker.MAX_PROBE_COUNT) {
            return doInvoke(request);
        } else {
            return doFallback();
        }
    }
}
