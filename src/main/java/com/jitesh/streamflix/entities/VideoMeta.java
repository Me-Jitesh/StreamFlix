package com.jitesh.streamflix.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video_metadata")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoMeta {
    @Id
    private String videoId;
    private String title;
    private String description;
    private String contentType;
    private String filePath;
}
