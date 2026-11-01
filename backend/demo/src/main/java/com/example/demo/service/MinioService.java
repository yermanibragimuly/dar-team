package com.example.demo.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName = "dar-bucket";

    @PostConstruct
    public void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании bucket: " + e.getMessage(), e);
        }
    }

    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "");
    }

    public String uploadFile(MultipartFile file, String folder) {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String objectName = folder.isEmpty() ? fileName : folder + "/" + fileName;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке файла в MinIO: " + e.getMessage(), e);
        }

        return "https://s3.dar-team.kz/" + bucketName + "/" + objectName;
    }

    public void deleteFileByUrl(String url) {
        if (url == null || url.isEmpty()) return;

        try {
            // Находим, где заканчивается бакет в ссылке
            // Ссылка: https://s3.dar-team.kz/dar-bucket/fighter/uuid-name.jpg
            String prefix = "/" + bucketName + "/";
            int index = url.indexOf(prefix);

            if (index != -1) {
                // Извлекаем: fighter/uuid-name.jpg
                String objectName = url.substring(index + prefix.length());

                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось удалить файл из MinIO: " + url, e);
            // Не кидаем RuntimeException, чтобы удаление сущности из БД не прервалось
        }
    }
}


