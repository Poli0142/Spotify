package com.music.catalog.repository;

import com.music.catalog.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtistId(Long artistId);

    boolean existsByArtistId(Long artistId);
}
