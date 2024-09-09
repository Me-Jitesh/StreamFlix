package com.jitesh.streamflix.controllers;

import com.jitesh.streamflix.entities.VideoMeta;
import com.jitesh.streamflix.services.VideoMetaService;
import com.jitesh.streamflix.utils.ResponseMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
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

    @GetMapping("/stream")
    public List<VideoMeta> getAllVideosMeta() {
        return vmService.getAllVideoMetas();
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId) {
        VideoMeta vm = vmService.getVideoMeta(videoId);

        String contentType = vm.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String path = vm.getFilePath();
        Resource video = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(video);
    }

    // Stream Video in Chunks(Byte Range)
    @GetMapping("/stream/range/{vidId}")
    public ResponseEntity<Resource> StreamVideoInChunks(@PathVariable String vidId, @RequestHeader(value = "Range", required = false) String range) {

        VideoMeta vm = vmService.getVideoMeta(vidId);

        String contentType = vm.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Path path = Paths.get(vm.getFilePath());
        Resource video = new FileSystemResource(path);

        if (range == null) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(video);
        }

        // Calculating Range

        long rangeStart;
        long rangeEnd;
        long fileLength = path.toFile().length();

        String[] ranges = range.replace("bytes=", "").split("-");

        rangeStart = Long.parseLong(ranges[0]);

        if (ranges.length > 1) {
            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            rangeEnd = fileLength - 1; // if end not present then send complete file
        }
        if (rangeEnd > fileLength - 1) {
            rangeEnd = fileLength - 1;
        }

        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        long contentLength = rangeEnd - rangeStart + 1;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.setContentLength(contentLength);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(inputStream));
    }
}
