package com.jitesh.streamflix.services;

import com.jitesh.streamflix.entities.Video;
import com.jitesh.streamflix.entities.VideoMeta;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoMetaService {
    VideoMeta saveVideoMeta(VideoMeta videoMeta, MultipartFile file, MultipartFile poster);

    VideoMeta getVideoMeta(String videoId);

    Video getVideo(String videoId);

    VideoMeta getVideoMetaByTitle(String videoTitle);

    List<VideoMeta> getAllVideoMetas();

    void deleteVideoMeta(String videoId);

    void deleteAllVideoMetas();

    void processVideo(String vidId);

    Video getThumb(String videoId);
}
