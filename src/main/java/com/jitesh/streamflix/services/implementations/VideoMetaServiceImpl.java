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
            // Transcoding Video for multiple resolution & Create HLS Segments
            processVideo(res.getVideoId());
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
        // path to store data:
        Path videoPath = Paths.get(filePath);

        // To Create Multiple Quality Videos(Resource Heavy)
//         String output360p = HLS_DIR + vidId + "/360p/";
//         String output720p = HLS_DIR + vidId + "/720p/";
//         String output1080p = HLS_DIR + vidId + "/1080p/";

        try {
//             Files.createDirectories(Paths.get(output360p));
//             Files.createDirectories(Paths.get(output720p));
//             Files.createDirectories(Paths.get(output1080p));

            Path outputPath = Paths.get(HLS_DIR, vidId);
            Path directory = Files.createDirectories(outputPath);
            // Set Permission to  dir
//            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-xr-x");
//            Files.setPosixFilePermissions(directory, permissions);
            System.out.println("OUTPUT PATH  " + outputPath);

            // Prepare ffmpeg Command
            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath);

            System.out.println("FFMPEG Command Created  " + ffmpegCmd);

            // StringBuilder ffmpegCmd = new StringBuilder();
            // ffmpegCmd.append("ffmpeg -i ")
            // .append(videoPath.toString())
            // .append(" -c:v libx264 -c:a aac")
            // .append(" ")
            // .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
            // .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
            // .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
            // .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
            // .append("-master_pl_name")
            // .append(HLS_DIR).append(vidId).append("/master.m3u8 ")
            // .append("-f hls -hls_time 10 -hls_list_size 0 ")
            // .append("-hls_segment_filename\"")
            // .append(HLS_DIR).append(vidId)
            // .append("/v%v/fileSequence%d.ts\" ")
            // .append("\"").append(HLS_DIR).append(vidId).append("/v%v/prog_index.m3u8\"");

            // Detect the operating system
            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("GET OS  " + os);

            // Command based on the OS
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);                 // Command for Windows
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Command for Linux/Mac
//                processBuilder = new ProcessBuilder("ffmpeg", "-version");
//                processBuilder.redirectErrorStream(true); // Redirect stderr to stdout
                processBuilder = new ProcessBuilder("bash", "-c", ffmpegCmd);
            } else {
                System.err.println("UNSUPPORTED OS  " + os);
                cleanupFiles(vidId);
                throw new UnsupportedOperationException("Unsupported Operating System:  " + os);
            }

            processBuilder.inheritIO();
            // Fire the Command
            Process process = processBuilder.start();
            System.out.println("FFMPEG Command Executed  Successfully");
            int exit = process.waitFor();
            if (exit != 0) {
                System.err.println("PROCESS EXIT " + exit);
                cleanupFiles(vidId);
                throw new RuntimeException("Video Processing Failed !");
            }

        } catch (IOException ex) {
            cleanupFiles(vidId);
            ex.printStackTrace();
            throw new RuntimeException("Video Processing Failed ! IO EXCEPTION!");
        } catch (Exception e) {
            cleanupFiles(vidId);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void cleanupFiles(String vidId) {
        // Delete Vid File
        File file = new File(getVideoMeta(vidId).getFilePath());
        if (file.exists() && file.delete()) {
            System.out.println("File Deleted");
        } else {
            System.err.println("File Deletion Failed");
        }
        // Delete DB Entry
        deleteVideoMeta(vidId);
        // Delete HLS_DIR
        new File(String.valueOf(Paths.get(HLS_DIR, vidId))).delete();
    }
}
