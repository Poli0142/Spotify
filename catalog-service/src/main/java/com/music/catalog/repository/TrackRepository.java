package com.music.catalog.repository;

import com.music.catalog.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByAlbumId(Long albumId);

    List<Track> findByTitleContainingIgnoreCase(String title);

    boolean existsByAlbumId(Long albumId);
}
