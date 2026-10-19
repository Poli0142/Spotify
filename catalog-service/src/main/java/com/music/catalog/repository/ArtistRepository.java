package com.music.catalog.repository;

import com.music.catalog.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByNameIgnoreCase(String name);

    Optional<Artist> findByUserId(Long userId);
}
