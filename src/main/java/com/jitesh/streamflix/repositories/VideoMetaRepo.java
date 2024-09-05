package com.jitesh.streamflix.repositories;

import com.jitesh.streamflix.entities.VideoMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoMetaRepo extends JpaRepository<VideoMeta, String> {
    Optional<VideoMeta> findByTitle(String title);
}
