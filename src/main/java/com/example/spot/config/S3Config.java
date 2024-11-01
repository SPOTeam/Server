package com.example.spot.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    /**
     * AmazonS3를 Bean으로 등록합니다.
     * @return AmazonS3
     */
    @Bean
    public AmazonS3 amazonS3() {
        // AWS 인증 정보를 이용하여 AmazonS3를 생성합니다.
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // AmazonS3를 생성합니다.
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }
}
