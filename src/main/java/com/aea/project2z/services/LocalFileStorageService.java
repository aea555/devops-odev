package com.aea.project2z.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    @Value("classpath:/static/images/")
    private Resource uploadDirResource;

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // Resolve the upload directory path
        Path uploadPath = Path.of(uploadDirResource.getURI());

        // Ensure that the directory exists, create it if necessary
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Construct the destination file path
        Path destFilePath = uploadPath.resolve(uniqueFileName);

        // Transfer the file to the destination
        file.transferTo(destFilePath);

        // Return the unique file name
        return "/images/" + uniqueFileName;
    }
}