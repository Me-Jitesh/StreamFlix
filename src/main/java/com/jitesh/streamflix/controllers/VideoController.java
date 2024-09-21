package com.jitesh.streamflix.controllers;

import com.jitesh.streamflix.entities.VideoMeta;
import com.jitesh.streamflix.entities.Visitor;
import com.jitesh.streamflix.services.implementations.VideoMetaService;
import com.jitesh.streamflix.services.implementations.VisitorService;
import com.jitesh.streamflix.utils.AppConstants;
import com.jitesh.streamflix.utils.IPLocation;
import com.jitesh.streamflix.utils.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
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
    private VisitorService visitorService;

    @Value("${files.hls-directory-path}")
    private String HLS_DIR;

    public VideoController(VideoMetaService vmService, VisitorService visitorService) {
        this.vmService = vmService;
        this.visitorService = visitorService;
    }

    @PostMapping
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile vidFile,
            @RequestParam("title") String title,
            @RequestParam("desc") String desc,
            @RequestParam("thumb") MultipartFile poster) {
        VideoMeta vm = new VideoMeta();
        vm.setVideoId(UUID.randomUUID().toString());
        vm.setTitle(title);
        vm.setDescription(desc);
        VideoMeta video = vmService.saveVideoMeta(vm, vidFile, poster);

        if (video != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseMessage.builder()
                    .message("Oops! Video Uploading Failed")
                    .success(false)
                    .build());
        }
    }

    @GetMapping("/stream")
    public List<VideoMeta> getAllVideosMeta(HttpServletRequest req) {
        Visitor visitor = IPLocation.extractIP(req);
        IPLocation.saveVisitor(req, visitorService, visitor);
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

    @GetMapping("/stream/thumb/{videoId}")
    public ResponseEntity<Resource> getPoster(@PathVariable String videoId) {
        VideoMeta vm = vmService.getVideoMeta(videoId);
        Resource poster = new FileSystemResource(vm.getThumbnailPath());
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(poster);
    }

    // Stream Video in Chunks(Byte Range)
    @GetMapping("/stream/range/{vidId}")
    public ResponseEntity<Resource> StreamVideoInChunks(@PathVariable String vidId,
            @RequestHeader(value = "Range", required = false) String range) {

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

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int) contentLength];
            inputStream.read(data, 0, data.length);

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
                    .body(new ByteArrayResource(data));

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Serve Master File
    @GetMapping("/stream/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serverMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HLS_DIR, videoId, "master.m3u8");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);

    }

    // Serve the Segments
    @GetMapping("/stream/{vidId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(@PathVariable String vidId, @PathVariable String segment) {

        // create path for segment
        Path path = Paths.get(HLS_DIR, vidId, segment + ".ts");

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);

    }

    @GetMapping("/stream/delete/{vidId}")
    public ResponseEntity<Resource> deleteVideo(@PathVariable String vidId) {
        vmService.deleteVideoMeta(vidId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stream/delete/all")
    public ResponseEntity<Resource> deleteAllVideos() {
        vmService.deleteAllVideoMetas();
        return ResponseEntity.ok().build();
    }
}
