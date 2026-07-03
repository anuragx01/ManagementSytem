package com.nexstar.portal.document.controller;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.document.service.S3DocumentService;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Document upload and management endpoints")
public class DocumentController {

    private final S3DocumentService s3DocumentService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/employee/{employeeId}/upload")
    @Operation(summary = "Upload a document for an employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadEmployeeDocument(
            @PathVariable UUID employeeId,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = s3DocumentService.uploadEmployeeDocument(
                    employeeId,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());

            return ResponseEntity.status(201).body(ApiResponse.created("Document uploaded successfully", result));
        } catch (IOException ex) {
            log.error("Failed to read uploaded file: {}", ex.getMessage());
            throw new BusinessException("Failed to read uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/task/{taskId}/upload")
    @Operation(summary = "Upload an attachment for a task")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadTaskAttachment(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = s3DocumentService.uploadTaskAttachment(
                    taskId,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());

            return ResponseEntity.status(201).body(ApiResponse.created("Task attachment uploaded successfully", result));
        } catch (IOException ex) {
            log.error("Failed to read uploaded task attachment: {}", ex.getMessage());
            throw new BusinessException("Failed to read uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/profile-picture")
    @Operation(summary = "Upload a profile picture for the current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            String fileUrl = s3DocumentService.uploadProfilePicture(
                    currentUser.getId(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType());

            // Update employee's profilePictureUrl
            employeeRepository.findByUserIdAndDeletedFalse(currentUser.getId()).ifPresent(emp -> {
                emp.setProfilePictureUrl(fileUrl);
                employeeRepository.save(emp);
            });

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("contentType", file.getContentType());

            return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully", result));
        } catch (IOException ex) {
            log.error("Failed to read profile picture upload: {}", ex.getMessage());
            throw new BusinessException("Failed to read uploaded file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Generate a presigned URL for a document (1 hour expiry)")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPresignedUrl(
            @RequestParam String key) {
        String url = s3DocumentService.generatePresignedUrl(key, Duration.ofHours(1));
        Map<String, String> result = new LinkedHashMap<>();
        result.put("presignedUrl", url);
        result.put("key", key);
        result.put("expiresInSeconds", String.valueOf(Duration.ofHours(1).getSeconds()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
