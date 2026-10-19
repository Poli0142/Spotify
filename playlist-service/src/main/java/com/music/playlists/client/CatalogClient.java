package com.music.playlists.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public CatalogClient(@LoadBalanced RestClient.Builder builder, CircuitBreakerFactory<?, ?> factory,
                         @Value("${clients.catalog-url}") String url) {
        this.restClient = builder.baseUrl(url).build();
        this.circuitBreaker = factory.create("catalog-service");
    }

    public TrackInfo getTrack(Long id) {
        return circuitBreaker.run(() -> restClient.get().uri("/api/tracks/{id}", id)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        return null;
                    }
                    if (response.getStatusCode().isError()) {
                        throw new IllegalStateException("catalog-service ответил " + response.getStatusCode());
                    }
                    TrackInfo t = response.bodyTo(TrackInfo.class);
                    return new TrackInfo(t.id(), t.title(), t.artistName(), t.albumTitle(), t.durationSec(),
                            t.soundcloudUrl(), true);
                }), e -> {
            log.warn("catalog-service недоступен, трек {}: {}", id, e.toString());
            return TrackInfo.unavailable(id);
        });
    }
}
