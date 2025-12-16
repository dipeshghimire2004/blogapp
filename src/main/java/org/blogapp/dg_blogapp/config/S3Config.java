package org.blogapp.dg_blogapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix="cloud.aws")
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${cloud.aws.region.static}")  // Matches application.properties
    private String region;

    @Value("${cloud.aws.s3.path-style-access:true}")
    private boolean pathStyleAccess;

    @Bean
    public S3Client s3Client(){

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);


        return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccess)
                        .build())
                .build();
    }

}


//Case A: You’re using LocalStack or MinIO
//
//👉 They require path-style access, meaning URLs look like:
//
//http://localhost:4566/my-bucket/my-file.jpg
//
//
//If pathStyleAccessEnabled(true) is not set, AWS SDK will use virtual-hosted style, which looks like:
//
//http://my-bucket.localhost:4566/my-file.jpg
//
//
//and that often fails locally.
//
//✅ So, for local testing (LocalStack/MinIO), you need it true.