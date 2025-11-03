package com.example.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private final Map<String, RequestTracker> requestCounts = new ConcurrentHashMap<>();
    private static final int LIMIT = 5; // Max requests per window
    private static final long WINDOW_MILLIS = 10000; // 10 seconds

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIP = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        RequestTracker tracker = requestCounts.computeIfAbsent(clientIP, k -> new RequestTracker());

        if (!tracker.allowRequest()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static class RequestTracker {
        private int count = 0;
        private long windowStart = Instant.now().toEpochMilli();

        public synchronized boolean allowRequest() {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart > WINDOW_MILLIS) {
                count = 0;
                windowStart = now;
            }
            count++;
            return count <= LIMIT;
        }
    }
}
