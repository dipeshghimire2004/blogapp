package org.blogapp.dg_blogapp.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

//    @Value("${cloud.aws.credentials.access-key}")
//    private String accessKey;
//
//    @Value("${cloud.aws.credentials.secret-key}")
//    private String secretKey;
//
//    @Value("${cloud.aws.s3.endpoint}")
//    private String s3Endpoint;
//
//    @Value("${cloud.aws.region.static}")  // Matches application.properties
//    private String region;

    private final String accessKey;
    private final String secretKey;
    private final String s3Endpoint;
    private final String region;

    public S3Config(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                    @Value("${cloud.aws.credentials.secret-key") String secretKey,
                    @Value("${cloud.aws.s3.endpoint}") String s3Endpoint,
                    @Value("${cloud.aws.region.static}") String region){
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.s3Endpoint = s3Endpoint;
        this.region = region;
    }

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, region))
                .withPathStyleAccessEnabled(true)
                .build();
    }

}