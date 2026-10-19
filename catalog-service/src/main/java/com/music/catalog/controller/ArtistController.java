package com.music.catalog.controller;

import com.music.catalog.dto.ArtistRequest;
import com.music.catalog.exception.NotFoundException;
import com.music.catalog.model.Album;
import com.music.catalog.model.Artist;
import com.music.catalog.repository.AlbumRepository;
import com.music.catalog.repository.ArtistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@Tag(name = "Исполнители")
public class ArtistController {

    private static final Logger log = LoggerFactory.getLogger(ArtistController.class);

    private final ArtistRepository artists;
    private final AlbumRepository albums;

    public ArtistController(ArtistRepository artists, AlbumRepository albums) {
        this.artists = artists;
        this.albums = albums;
    }

    @GetMapping
    @Operation(summary = "Все исполнители")
    public List<Artist> getAll() {
        log.info("Список исполнителей");
        return artists.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Исполнитель по id")
    public Artist getById(@PathVariable Long id) {
        log.info("Исполнитель {}", id);
        return artists.findById(id).orElseThrow(() -> new NotFoundException("Исполнитель " + id + " не найден"));
    }

    @GetMapping("/{id}/albums")
    @Operation(summary = "Альбомы исполнителя")
    public List<Album> getAlbums(@PathVariable Long id) {
        getById(id);
        return albums.findByArtistId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить исполнителя")
    public Artist create(@Valid @RequestBody ArtistRequest request) {
        Artist artist = new Artist();
        artist.setName(request.name());
        artist.setGenre(request.genre());
        artist.setCountry(request.country());
        artist = artists.save(artist);
        log.info("Добавлен исполнитель {} ({})", artist.getId(), artist.getName());
        return artist;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить исполнителя")
    public void delete(@PathVariable Long id) {
        artists.delete(getById(id));
        log.info("Удалён исполнитель {}", id);
    }
}
