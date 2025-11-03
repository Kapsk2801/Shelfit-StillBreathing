package com.example.api_gateway.filters;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                HttpStatusCode statusCode = originalResponse.getStatusCode();
                int code = (statusCode != null) ? statusCode.value() : 500;

                // ✅ For all error responses, return JSON instead of Whitelabel
                if (code >= 400) {
                    String errorText;
                    switch (code) {
                        case 400 -> errorText = "Bad Request";
                        case 401 -> errorText = "Unauthorized";
                        case 403 -> errorText = "Forbidden";
                        case 404 -> errorText = "Not Found";
                        case 500 -> errorText = "Internal Server Error";
                        default -> errorText = "Unexpected Error";
                    }

                    String json = String.format(
                            "{\"status\": %d, \"error\": \"%s\", \"message\": \"An error occurred while processing your request.\"}",
                            code, errorText
                    );

                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = bufferFactory.wrap(bytes);

                    originalResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    originalResponse.getHeaders().setContentLength(bytes.length);

                    return super.writeWith(Mono.just(buffer));
                }

                // ✅ Normal successful case
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
