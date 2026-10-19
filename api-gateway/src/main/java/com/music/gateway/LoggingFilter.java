package com.music.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        var request = exchange.getRequest();
        log.info("{} {}", request.getMethod(), request.getURI().getPath());
        return chain.filter(exchange).then(Mono.fromRunnable(() ->
                log.info("{} {} {} {}ms", request.getMethod(), request.getURI().getPath(),
                        exchange.getResponse().getStatusCode(), System.currentTimeMillis() - start)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
