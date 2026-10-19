package com.music.streaming.controller;

import com.music.streaming.client.CatalogClient;
import com.music.streaming.client.TrackInfo;
import com.music.streaming.client.UserClient;
import com.music.streaming.dto.PlayRequest;
import com.music.streaming.dto.PlayResponse;
import com.music.streaming.dto.TopTrack;
import com.music.streaming.exception.NotFoundException;
import com.music.streaming.model.ListeningHistory;
import com.music.streaming.repository.ListeningHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/stream")
@Tag(name = "Прослушивания")
public class StreamController {

    private static final Logger log = LoggerFactory.getLogger(StreamController.class);

    private final ListeningHistoryRepository history;
    private final UserClient userClient;
    private final CatalogClient catalogClient;

    public StreamController(ListeningHistoryRepository history, UserClient userClient, CatalogClient catalogClient) {
        this.history = history;
        this.userClient = userClient;
        this.catalogClient = catalogClient;
    }

    @PostMapping("/play")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Прослушать трек",
            description = "Проверяет пользователя и трек через Circuit Breaker. Если сервис недоступен, прослушивание сохраняется с degraded = true")
    public PlayResponse play(@Valid @RequestBody PlayRequest request) {
        Boolean userExists = userClient.exists(request.userId());
        if (Boolean.FALSE.equals(userExists)) {
            throw new NotFoundException("Пользователь " + request.userId() + " не найден");
        }
        TrackInfo track = catalogClient.getTrack(request.trackId());
        if (track == null) {
            throw new NotFoundException("Трек " + request.trackId() + " не найден");
        }
        ListeningHistory saved = history.save(new ListeningHistory(request.userId(), request.trackId()));
        boolean degraded = userExists == null || !track.available();
        log.info("Пользователь {} слушает трек {}, degraded: {}", request.userId(), request.trackId(), degraded);
        return new PlayResponse(saved.getId(), saved.getUserId(), track, saved.getPlayedAt(), degraded);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "История прослушиваний пользователя")
    public List<PlayResponse> history(@PathVariable Long userId) {
        log.info("История пользователя {}", userId);
        return history.findByUserIdOrderByPlayedAtDesc(userId).stream()
                .map(h -> {
                    TrackInfo track = catalogClient.getTrack(h.getTrackId());
                    return track == null ? null : new PlayResponse(h.getId(), h.getUserId(), track, h.getPlayedAt(), !track.available());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @GetMapping("/top")
    @Operation(summary = "Топ треков по прослушиваниям")
    public List<TopTrack> top(@RequestParam(defaultValue = "5") int limit) {
        log.info("Топ {} треков", limit);
        return history.findTop(PageRequest.of(0, limit)).stream()
                .map(t -> new TopTrack(catalogClient.getTrack(t.getTrackId()), t.getPlays()))
                .filter(t -> t.track() != null)
                .toList();
    }
}
