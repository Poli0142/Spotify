package com.music.playlists.controller;

import com.music.playlists.client.CatalogClient;
import com.music.playlists.client.TrackInfo;
import com.music.playlists.dto.AddTrackRequest;
import com.music.playlists.dto.PlaylistDetails;
import com.music.playlists.dto.PlaylistRequest;
import com.music.playlists.exception.ConflictException;
import com.music.playlists.exception.NotFoundException;
import com.music.playlists.exception.ServiceUnavailableException;
import com.music.playlists.model.Playlist;
import com.music.playlists.model.PlaylistTrack;
import com.music.playlists.repository.PlaylistRepository;
import com.music.playlists.repository.PlaylistTrackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/playlists")
@Tag(name = "Плейлисты")
public class PlaylistController {

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    private final PlaylistRepository playlists;
    private final PlaylistTrackRepository playlistTracks;
    private final CatalogClient catalogClient;

    public PlaylistController(PlaylistRepository playlists, PlaylistTrackRepository playlistTracks,
                              CatalogClient catalogClient) {
        this.playlists = playlists;
        this.playlistTracks = playlistTracks;
        this.catalogClient = catalogClient;
    }

    @GetMapping
    @Operation(summary = "Все плейлисты или плейлисты пользователя")
    public List<Playlist> getAll(@Parameter(description = "id пользователя") @RequestParam(required = false) Long userId) {
        log.info("Список плейлистов, userId: {}", userId);
        return userId == null ? playlists.findAll() : playlists.findByUserId(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Плейлист с треками", description = "Треки запрашиваются у catalog-service через Circuit Breaker")
    public PlaylistDetails getById(@PathVariable Long id) {
        log.info("Плейлист {}", id);
        Playlist playlist = find(id);
        List<TrackInfo> tracks = playlistTracks.findByPlaylistIdOrderByAddedAt(id).stream()
                .map(pt -> catalogClient.getTrack(pt.getTrackId()))
                .filter(Objects::nonNull)
                .toList();
        return PlaylistDetails.of(playlist, tracks);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать плейлист")
    public Playlist create(@Valid @RequestBody PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        playlist.setUserId(request.userId());
        playlist = playlists.save(playlist);
        log.info("Создан плейлист {} ({})", playlist.getId(), playlist.getName());
        return playlist;
    }

    @PostMapping("/{id}/tracks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить трек в плейлист", description = "Трек проверяется в catalog-service через Circuit Breaker")
    public PlaylistDetails addTrack(@PathVariable Long id, @Valid @RequestBody AddTrackRequest request) {
        find(id);
        TrackInfo track = catalogClient.getTrack(request.trackId());
        if (track == null) {
            throw new NotFoundException("Трек " + request.trackId() + " не найден");
        }
        if (!track.available()) {
            throw new ServiceUnavailableException("Каталог недоступен, трек нельзя проверить");
        }
        if (playlistTracks.existsByPlaylistIdAndTrackId(id, request.trackId())) {
            throw new ConflictException("Трек уже есть в плейлисте");
        }
        playlistTracks.save(new PlaylistTrack(id, request.trackId()));
        log.info("В плейлист {} добавлен трек {}", id, request.trackId());
        return getById(id);
    }

    @DeleteMapping("/{id}/tracks/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Убрать трек из плейлиста")
    public void removeTrack(@PathVariable Long id, @PathVariable Long trackId) {
        if (playlistTracks.deleteByPlaylistIdAndTrackId(id, trackId) == 0) {
            throw new NotFoundException("Трека " + trackId + " нет в плейлисте " + id);
        }
        log.info("Из плейлиста {} убран трек {}", id, trackId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить плейлист")
    public void delete(@PathVariable Long id) {
        playlists.delete(find(id));
        log.info("Удалён плейлист {}", id);
    }

    private Playlist find(Long id) {
        return playlists.findById(id).orElseThrow(() -> new NotFoundException("Плейлист " + id + " не найден"));
    }
}
