package com.music.catalog.controller;

import com.music.catalog.dto.PublishRequest;
import com.music.catalog.dto.TrackDto;
import com.music.catalog.dto.TrackRequest;
import com.music.catalog.exception.NotFoundException;
import com.music.catalog.model.Album;
import com.music.catalog.model.Artist;
import com.music.catalog.model.Track;
import com.music.catalog.repository.AlbumRepository;
import com.music.catalog.repository.ArtistRepository;
import com.music.catalog.repository.TrackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@Tag(name = "Треки")
public class TrackController {

    private static final Logger log = LoggerFactory.getLogger(TrackController.class);

    private final TrackRepository tracks;
    private final AlbumRepository albums;
    private final ArtistRepository artists;

    public TrackController(TrackRepository tracks, AlbumRepository albums, ArtistRepository artists) {
        this.tracks = tracks;
        this.albums = albums;
        this.artists = artists;
    }

    @GetMapping
    @Operation(summary = "Все треки или поиск по названию")
    public List<TrackDto> getAll(@Parameter(description = "Часть названия") @RequestParam(required = false) String search) {
        log.info("Список треков, поиск: {}", search);
        List<Track> result = search == null || search.isBlank() ? tracks.findAll() : tracks.findByTitleContainingIgnoreCase(search);
        return result.stream().map(TrackDto::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Трек по id")
    public TrackDto getById(@PathVariable Long id) {
        log.info("Трек {}", id);
        return TrackDto.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить трек")
    public TrackDto create(@Valid @RequestBody TrackRequest request) {
        Track track = new Track();
        track.setTitle(request.title());
        track.setDurationSec(request.durationSec());
        track.setSoundcloudUrl(request.soundcloudUrl());
        track.setAlbum(albums.findById(request.albumId())
                .orElseThrow(() -> new NotFoundException("Альбом " + request.albumId() + " не найден")));
        track = tracks.save(track);
        log.info("Добавлен трек {} ({})", track.getId(), track.getTitle());
        return TrackDto.from(track);
    }

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Опубликовать трек от имени исполнителя",
            description = "Исполнитель ищется по аккаунту, затем по имени, иначе создаётся. Трек публикуется как сингл")
    public TrackDto publish(@Valid @RequestBody PublishRequest request) {
        String name = request.artistName().trim();
        Artist artist = artists.findByUserId(request.userId())
                .or(() -> artists.findByNameIgnoreCase(name).filter(a -> a.getUserId() == null))
                .orElseGet(Artist::new);
        if (artist.getId() == null) {
            artist.setName(name);
            artist.setGenre(request.genre());
        }
        artist.setUserId(request.userId());
        artist = artists.save(artist);
        Album album = new Album();
        album.setTitle(request.title());
        album.setReleaseYear(Year.now().getValue());
        album.setArtist(artist);
        album = albums.save(album);
        Track track = new Track();
        track.setTitle(request.title());
        track.setDurationSec(request.durationSec());
        track.setSoundcloudUrl(request.soundcloudUrl());
        track.setAlbum(album);
        track = tracks.save(track);
        log.info("Опубликован трек {} ({} - {})", track.getId(), artist.getName(), track.getTitle());
        return TrackDto.from(track);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Удалить трек", description = "Пустой альбом и исполнитель без альбомов удаляются вместе с треком")
    public void delete(@PathVariable Long id) {
        Track track = find(id);
        Album album = track.getAlbum();
        tracks.delete(track);
        tracks.flush();
        if (!tracks.existsByAlbumId(album.getId())) {
            albums.delete(album);
            albums.flush();
            if (!albums.existsByArtistId(album.getArtist().getId())) {
                artists.delete(album.getArtist());
            }
        }
        log.info("Удалён трек {}", id);
    }

    private Track find(Long id) {
        return tracks.findById(id).orElseThrow(() -> new NotFoundException("Трек " + id + " не найден"));
    }
}
