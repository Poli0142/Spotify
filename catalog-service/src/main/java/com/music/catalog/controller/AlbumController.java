package com.music.catalog.controller;

import com.music.catalog.dto.AlbumRequest;
import com.music.catalog.dto.TrackDto;
import com.music.catalog.exception.NotFoundException;
import com.music.catalog.model.Album;
import com.music.catalog.repository.AlbumRepository;
import com.music.catalog.repository.ArtistRepository;
import com.music.catalog.repository.TrackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@Tag(name = "Альбомы")
public class AlbumController {

    private static final Logger log = LoggerFactory.getLogger(AlbumController.class);

    private final AlbumRepository albums;
    private final ArtistRepository artists;
    private final TrackRepository tracks;

    public AlbumController(AlbumRepository albums, ArtistRepository artists, TrackRepository tracks) {
        this.albums = albums;
        this.artists = artists;
        this.tracks = tracks;
    }

    @GetMapping
    @Operation(summary = "Все альбомы")
    public List<Album> getAll() {
        log.info("Список альбомов");
        return albums.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Альбом по id")
    public Album getById(@PathVariable Long id) {
        log.info("Альбом {}", id);
        return albums.findById(id).orElseThrow(() -> new NotFoundException("Альбом " + id + " не найден"));
    }

    @GetMapping("/{id}/tracks")
    @Operation(summary = "Треки альбома")
    public List<TrackDto> getTracks(@PathVariable Long id) {
        getById(id);
        return tracks.findByAlbumId(id).stream().map(TrackDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить альбом")
    public Album create(@Valid @RequestBody AlbumRequest request) {
        Album album = new Album();
        album.setTitle(request.title());
        album.setReleaseYear(request.releaseYear());
        album.setArtist(artists.findById(request.artistId())
                .orElseThrow(() -> new NotFoundException("Исполнитель " + request.artistId() + " не найден")));
        album = albums.save(album);
        log.info("Добавлен альбом {} ({})", album.getId(), album.getTitle());
        return album;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить альбом")
    public void delete(@PathVariable Long id) {
        albums.delete(getById(id));
        log.info("Удалён альбом {}", id);
    }
}
