package com.nexstar.portal.document.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3DocumentService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AppProperties appProperties;

    public String uploadFile(String key, InputStream content, long size, String contentType) {
        String bucket = appProperties.getAws().getS3Bucket();
        String region = appProperties.getAws().getRegion();

        if (bucket == null || bucket.isBlank()) {
            log.warn("S3 bucket not configured — returning mock URL for key: {}", key);
            return "https://mock-s3-local/" + key;
        }

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(content, size));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
        } catch (S3Exception ex) {
            log.error("S3 upload failed for key {}: {}", key, ex.getMessage());
            throw new BusinessException("File upload failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error("Unexpected error uploading to S3 key {}: {}", key, ex.getMessage());
            throw new BusinessException("File upload failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteFile(String key) {
        String bucket = appProperties.getAws().getS3Bucket();
        if (bucket == null || bucket.isBlank()) {
            log.warn("S3 bucket not configured — skipping delete for key: {}", key);
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("Deleted S3 object: {}", key);
        } catch (S3Exception ex) {
            log.error("S3 delete failed for key {}: {}", key, ex.getMessage());
            throw new BusinessException("File deletion failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String generatePresignedUrl(String key, Duration expiry) {
        String bucket = appProperties.getAws().getS3Bucket();
        if (bucket == null || bucket.isBlank()) {
            log.warn("S3 bucket not configured — returning mock presigned URL for key: {}", key);
            return "https://mock-s3-local/presigned/" + key + "?expires=" + expiry.getSeconds();
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        } catch (S3Exception ex) {
            log.error("S3 presign failed for key {}: {}", key, ex.getMessage());
            throw new BusinessException("Failed to generate presigned URL: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadEmployeeDocument(UUID employeeId, String fileName, InputStream content, long size, String contentType) {
        String sanitizedFileName = sanitizeFileName(fileName);
        String key = String.format("employees/%s/documents/%s/%s", employeeId, UUID.randomUUID(), sanitizedFileName);
        return uploadFile(key, content, size, contentType);
    }

    public String uploadTaskAttachment(UUID taskId, String fileName, InputStream content, long size, String contentType) {
        String sanitizedFileName = sanitizeFileName(fileName);
        String key = String.format("tasks/%s/attachments/%s/%s", taskId, UUID.randomUUID(), sanitizedFileName);
        return uploadFile(key, content, size, contentType);
    }

    public String uploadProfilePicture(UUID userId, InputStream content, long size, String contentType) {
        String key = String.format("profile-pictures/%s/%s", userId, UUID.randomUUID());
        return uploadFile(key, content, size, contentType);
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
