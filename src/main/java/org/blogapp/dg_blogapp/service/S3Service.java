package org.blogapp.dg_blogapp.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Service for interaction with Amazon aws to upload the manage files
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final String s3Endpoint;

    // Constructor-based initialization (Better than @Bean method)
    @Autowired
    public S3Service(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket-name}") String bucketName,
                     @Value("${cloud.aws.s3.endpoint}") String s3Endpoint) {

        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        this.s3Endpoint = s3Endpoint;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing S3 Bucket with endpoint :{} and bucket:{} ",s3Endpoint, bucketName);
        createBucketIfNotExists();
    }

    /**
     * creates the S3 bucket if it does not already exist.
     */
    public void createBucketIfNotExists() {
        try{
            if (!amazonS3.doesBucketExistV2(bucketName)) {
                amazonS3.createBucket(bucketName);
                logger.info("Bucket created: {}", bucketName);
            } else {
                logger.info("Bucket already exists: {}", bucketName);
            }
        }
        catch(Exception e){
            logger.error("Failed to create or verify bucket{} :{}",bucketName, e.getMessage());
            throw new RuntimeException("Failed to create or verify bucket" + bucketName, e);
        }
    }

    /**
     * uploads the file to S3 and returns the public URL
     * @param file the file to upload
     * @return the URL of the updated file
     * @throws RuntimeException if the upload image fails
     */
    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        logger.info("Uploading file: {} to bucket: {}", fileName, bucketName);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            String fileUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, fileName);
            logger.info("File uploaded to S3 bucket: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            logger.error("Error while uploading file {} : {}",fileName, e.getMessage());
            throw new RuntimeException("Error while uploading file", e);
//        }catch(Exception e){
//            logger.error("Error while uploading file {}: {}", fileName, e.getMessage());
//            throw new RuntimeException("Error while uploading file", e);
        }
    }
}
//        File convertedFile = convertMultiPartToFile(file);
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
//
//
//        // Delete temporary file safely
//        if (!convertedFile.delete()) {
//            logger.warn("Temporary file could not be deleted: {}", convertedFile.getAbsolutePath());
//        }
//
//        return s3Endpoint+"/" + bucketName + "/" + fileName;
//    }

//}
