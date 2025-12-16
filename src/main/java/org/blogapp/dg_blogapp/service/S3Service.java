package org.blogapp.dg_blogapp.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


/**
 * Service for interaction with Amazon aws to upload the manage files
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;


    @PostConstruct
    public void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info(" Bucket '{}' already exists", bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info(" Bucket '{}' not found, creating...", bucketName);
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
            } else {
                throw e;
            }
        }
    }

    public void uploadFileIntoS3(MultipartFile file, String key) {
        try {
            log.info("Uploading image into s3");
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
        } catch (Exception e) {
            log.error("unable to upload image:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}














//    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
//
//    private final AmazonS3 amazonS3;
//    private final String bucketName;
//    private final String s3Endpoint;
//
//    // Constructor-based initialization (Better than @Bean method)
//    @Autowired
//    public S3Service(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket-name}") String bucketName,
//                     @Value("${cloud.aws.s3.endpoint}") String s3Endpoint) {
//
//        this.amazonS3 = amazonS3;
//        this.bucketName = bucketName;
//        this.s3Endpoint = s3Endpoint;
//    }
//
//    @PostConstruct
//    public void init() {
//        logger.info("Initializing S3 Bucket with endpoint :{} and bucket:{} ",s3Endpoint, bucketName);
//        createBucketIfNotExists();
//    }
//
//    /**
//     * creates the S3 bucket if it does not already exist.
//     */
//    public void createBucketIfNotExists() {
//        try{
//            if (!amazonS3.doesBucketExistV2(bucketName)) {
//                amazonS3.createBucket(bucketName);
//                logger.info("Bucket created: {}", bucketName);
//            } else {
//                logger.info("Bucket already exists: {}", bucketName);
//            }
//        }
//        catch(Exception e){
//            logger.error("Failed to create or verify bucket{} :{}",bucketName, e.getMessage());
//            throw new RuntimeException("Failed to create or verify bucket" + bucketName, e);
//        }
//    }
//
//    /**
//     * uploads the file to S3 and returns the public URL
//     * @param file the file to upload
//     * @return the URL of the updated file
//     * @throws RuntimeException if the upload image fails
//     */
//    public String uploadFile(MultipartFile file) {
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        logger.info("Uploading file: {} to bucket: {}", fileName, bucketName);
//
//        try (InputStream inputStream = file.getInputStream()) {
//            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentLength(file.getSize());
//            metadata.setContentType(file.getContentType());
//
//            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
//            String fileUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, fileName);
//            logger.info("File uploaded to S3 bucket: {}", fileUrl);
//            return fileUrl;
//
//        } catch (IOException e) {
//            logger.error("Error while uploading file {} : {}",fileName, e.getMessage());
//            throw new RuntimeException("Error while uploading file", e);
//        }
//    }
