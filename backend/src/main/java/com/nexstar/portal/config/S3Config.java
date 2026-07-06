package com.nexstar.portal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final AppProperties props;

    @Bean
    public S3Client s3Client() {
        String region = props.getAws().getRegion();
        String accessKey = props.getAws().getAccessKey();
        String secretKey = props.getAws().getSecretKey();

        if (region == null || region.isBlank() || accessKey == null || accessKey.isBlank()) {
            // Return a no-op client builder that will fail gracefully at call time
            // Using ap-southeast-1 as fallback region for bean initialization
            region = "us-east-1";
            accessKey = "PLACEHOLDER";
            secretKey = "PLACEHOLDER";
        }

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        String region = props.getAws().getRegion();
        String accessKey = props.getAws().getAccessKey();
        String secretKey = props.getAws().getSecretKey();

        if (region == null || region.isBlank() || accessKey == null || accessKey.isBlank()) {
            region = "us-east-1";
            accessKey = "PLACEHOLDER";
            secretKey = "PLACEHOLDER";
        }

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
