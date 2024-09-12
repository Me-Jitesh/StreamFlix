package com.jitesh.streamflix.services.implementations;

import com.jitesh.streamflix.entities.VideoMeta;
import com.jitesh.streamflix.repositories.VideoMetaRepo;
import com.jitesh.streamflix.services.VideoMetaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoMetaServiceImpl implements VideoMetaService {

    @Value("${files.directory-path}")
    private String VID_DIR;

    @Value("${files.hls-directory-path}")
    private String HLS_DIR;

    @Autowired
    private VideoMetaRepo vmRepo;

    @PostConstruct
    public void init() {
        File file_vid = new File(VID_DIR);
        if (!file_vid.exists()) {
            file_vid.mkdir();
            System.out.println("Video Directory Created");
        }

        File file_hls = new File(HLS_DIR);
        if (!file_hls.exists()) {
            file_hls.mkdir();
            System.out.println("HLS Directory Created");
        }
    }

    @Override
    public VideoMeta saveVideoMeta(VideoMeta vm, MultipartFile file) {
        try {
            // Fetching data
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            // Cleaning
            String filename = file.getOriginalFilename();
            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanFolder = StringUtils.cleanPath(VID_DIR);
            Path path = Paths.get(cleanFolder, cleanFileName);
            System.out.println(path); // Logging

            // Storing
            vm.setContentType(contentType);
            vm.setFilePath(path.toString());
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING); // Writing video to directory
            VideoMeta res = vmRepo.save(vm);
            // Transcoding  Video for multiple resolution &  Create HLS Segments
            processVideo(res.getVideoId());
            //TODO:  Delete actual video file and database entry if exception
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public VideoMeta getVideoMeta(String videoId) {
        return vmRepo.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public VideoMeta getVideoMetaByTitle(String videoTitle) {
        return vmRepo.findByTitle(videoTitle).orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public List<VideoMeta> getAllVideoMetas() {
        return vmRepo.findAll();
    }

    @Override
    public void deleteVideoMeta(String videoId) {
        vmRepo.delete(getVideoMeta(videoId));
    }

    public void deleteAllVideoMetas() {
        vmRepo.deleteAll();
    }

    @Override
    public void processVideo(String vidId) {

        VideoMeta video = getVideoMeta(vidId);
        String filePath = video.getFilePath();

        // path where to store data:
        Path videoPath = Paths.get(filePath);

//         String output360p = HSL_DIR + vidId + "/360p/";
//         String output720p = HSL_DIR + vidId + "/720p/";
//         String output1080p = HSL_DIR + vidId + "/1080p/";

        try {
//             Files.createDirectories(Paths.get(output360p));
//             Files.createDirectories(Paths.get(output720p));
//             Files.createDirectories(Paths.get(output1080p));

            // ffmpeg command
            Path outputPath = Paths.get(HLS_DIR, vidId);

            Files.createDirectories(outputPath);

            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath);

//             StringBuilder ffmpegCmd = new StringBuilder();
//             ffmpegCmd.append("ffmpeg -i ")
//             .append(videoPath.toString())
//             .append(" -c:v libx264 -c:a aac")
//             .append(" ")
//             .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//             .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//             .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//             .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//             .append("-master_pl_name")
//             .append(HLS_DIR).append(vidId).append("/master.m3u8 ")
//             .append("-f hls -hls_time 10 -hls_list_size 0 ")
//             .append("-hls_segment_filename\"")
//             .append(HLS_DIR).append(vidId)
//             .append("/v%v/fileSequence%d.ts\" ")
//             .append("\"").append(HLS_DIR).append(vidId).append("/v%v/prog_index.m3u8\"");

            // fire this command
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd); // Windows
//            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd); // Linux

            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Video Processing Failed !");
            }

        } catch (IOException ex) {
            throw new RuntimeException("Video Processing Fail !");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
