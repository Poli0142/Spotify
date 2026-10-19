package com.music.streaming.controller;

import com.music.streaming.client.CatalogClient;
import com.music.streaming.client.TrackInfo;
import com.music.streaming.dto.LikeRequest;
import com.music.streaming.exception.NotFoundException;
import com.music.streaming.model.Like;
import com.music.streaming.repository.LikeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/stream/likes")
@Tag(name = "Любимые треки")
public class LikeController {

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    private final LikeRepository likes;
    private final CatalogClient catalogClient;

    public LikeController(LikeRepository likes, CatalogClient catalogClient) {
        this.likes = likes;
        this.catalogClient = catalogClient;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Любимые треки пользователя", description = "Данные о треках берутся у catalog-service через Circuit Breaker")
    public List<TrackInfo> getAll(@PathVariable Long userId) {
        log.info("Любимые треки пользователя {}", userId);
        return likes.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(l -> catalogClient.getTrack(l.getTrackId()))
                .filter(Objects::nonNull)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Лайкнуть трек")
    public void like(@Valid @RequestBody LikeRequest request) {
        if (!likes.existsByUserIdAndTrackId(request.userId(), request.trackId())) {
            likes.save(new Like(request.userId(), request.trackId()));
            log.info("Пользователь {} лайкнул трек {}", request.userId(), request.trackId());
        }
    }

    @DeleteMapping("/{userId}/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Убрать лайк")
    public void unlike(@PathVariable Long userId, @PathVariable Long trackId) {
        if (likes.deleteByUserIdAndTrackId(userId, trackId) == 0) {
            throw new NotFoundException("Лайк не найден");
        }
        log.info("Пользователь {} убрал лайк с трека {}", userId, trackId);
    }
}
