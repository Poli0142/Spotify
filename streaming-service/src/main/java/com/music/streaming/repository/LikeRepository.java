package com.music.streaming.repository;

import com.music.streaming.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {

    List<Like> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndTrackId(Long userId, Long trackId);

    @Transactional
    long deleteByUserIdAndTrackId(Long userId, Long trackId);
}
