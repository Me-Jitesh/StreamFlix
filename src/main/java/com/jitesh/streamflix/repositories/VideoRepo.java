package com.jitesh.streamflix.repositories;

import com.jitesh.streamflix.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepo extends JpaRepository<Video, String> {
}
