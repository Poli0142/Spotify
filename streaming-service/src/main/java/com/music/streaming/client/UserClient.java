package com.music.streaming.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public UserClient(@LoadBalanced RestClient.Builder builder, CircuitBreakerFactory<?, ?> factory,
                      @Value("${clients.user-url}") String url) {
        this.restClient = builder.baseUrl(url).build();
        this.circuitBreaker = factory.create("user-service");
    }

    public Boolean exists(Long id) {
        return circuitBreaker.run(() -> restClient.get().uri("/api/users/{id}", id)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        return false;
                    }
                    if (response.getStatusCode().isError()) {
                        throw new IllegalStateException("user-service ответил " + response.getStatusCode());
                    }
                    return true;
                }), e -> {
            log.warn("user-service недоступен, пользователь {}: {}", id, e.toString());
            return null;
        });
    }
}
