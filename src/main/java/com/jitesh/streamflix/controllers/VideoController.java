package com.jitesh.streamflix.controllers;

import com.jitesh.streamflix.entities.VideoMeta;
import com.jitesh.streamflix.services.VideoMetaService;
import com.jitesh.streamflix.utils.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private VideoMetaService vmService;

    public VideoController(VideoMetaService vmService) {
        this.vmService = vmService;
    }

    @PostMapping
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile vidFile,
            @RequestParam("title") String title,
            @RequestParam("desc") String desc
    ) {
        VideoMeta vm = new VideoMeta();
        vm.setVideoId(UUID.randomUUID().toString());
        vm.setTitle(title);
        vm.setDescription(desc);
        VideoMeta video = vmService.saveVideoMeta(vm, vidFile);

        if (video != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseMessage.builder()
                    .message("Oops! Video Uploading Failed")
                    .success(false)
                    .build()
            );
        }
    }
}
