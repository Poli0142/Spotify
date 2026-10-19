package com.music.playlists.repository;

import com.music.playlists.model.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    List<PlaylistTrack> findByPlaylistIdOrderByAddedAt(Long playlistId);

    boolean existsByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    @Transactional
    long deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);
}
