package com.example.demo.controller;

import com.example.demo.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final MinioService minioService;

    @PostMapping("/{folder}")
    public ResponseEntity<String> uploadImage(
            @PathVariable String folder,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadFile(file, folder);
        return ResponseEntity.ok(imageUrl);
    }
}
