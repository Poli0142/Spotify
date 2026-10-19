package com.music.gateway;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Hidden
@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/fallback/{service}")
    public Mono<ResponseEntity<Map<String, Object>>> fallback(@PathVariable String service) {
        log.warn("fallback {}", service);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "service", service,
                "status", 503,
                "message", service + " недоступен",
                "timestamp", LocalDateTime.now().toString())));
    }
}
