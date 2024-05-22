package com.aea.project2z.services;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
@Profile("prod")
public class AmazonS3FileStorageService implements FileStorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    @Autowired
    public AmazonS3FileStorageService(@Value("${aws.bucket.name}") String bucketName,
                                      @Value("${aws.accessKey}") String accessKey,
                                      @Value("${aws.secretKey}") String secretKey) {
        this.bucketName = bucketName;
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = generateUniqueFileName(originalFileName);

        // Read the contents of the input stream into a byte array
        byte[] fileBytes = file.getBytes();

        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(fileBytes);

        // Upload the object to Amazon S3
        amazonS3.putObject(bucketName, uniqueFileName, resource.getInputStream(), null);

        return "https://" + bucketName + ".s3." + "eu-central-1" + ".amazonaws.com/" + uniqueFileName;
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}

