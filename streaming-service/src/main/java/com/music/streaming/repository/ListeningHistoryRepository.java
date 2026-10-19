package com.music.streaming.repository;

import com.music.streaming.model.ListeningHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {

    List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId);

    @Query("select h.trackId as trackId, count(h) as plays from ListeningHistory h group by h.trackId order by count(h) desc")
    List<TrackPlays> findTop(Pageable pageable);

    interface TrackPlays {
        Long getTrackId();
        Long getPlays();
    }
}
