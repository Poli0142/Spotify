package com.music.playlists.repository;

import com.music.playlists.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserId(Long userId);
}
