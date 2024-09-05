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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoMetaServiceImpl implements VideoMetaService {

    @Value("${files.directory-path}")
    private String DirectoryPath;
    @Autowired
    private VideoMetaRepo vmRepo;

    @PostConstruct
    public void init() {
        File file = new File(DirectoryPath);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("Directory Created");
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
            String cleanFolder = StringUtils.cleanPath(DirectoryPath);
            Path path = Paths.get(cleanFolder, cleanFileName);
            System.out.println(path); // Logging

            // Storing
            vm.setContentType(contentType);
            vm.setFilePath(path.toString());
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);     // Writing video to directory
            return vmRepo.save(vm);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public VideoMeta getVideoMeta(String videoId) {
        return null;
    }

    @Override
    public VideoMeta getVideoMetaByTitle(String videoTitle) {
        return null;
    }

    @Override
    public List<VideoMeta> getAllVideoMetas() {
        return null;
    }
}
