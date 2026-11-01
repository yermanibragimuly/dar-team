package com.example.demo.controller;

import com.example.demo.dto.video.VideoDTO;
import com.example.demo.dto.video.VideoSubscriptionDTO;
import com.example.demo.service.video.VideoService;
import com.example.demo.service.video.VideoSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/v1/subscriptions")
@RequiredArgsConstructor
public class VideoSubscriptionController {

    private final VideoSubscriptionService subscriptionService;
    private final VideoService videoService;

    @PostMapping
    public ResponseEntity<?> subscribeToVideo(@RequestBody VideoSubscriptionDTO request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ошибка: пользователь не авторизован.");
        }

        try {
            String username = principal.getName();
            // Достаем videoId из тела запроса
            VideoSubscriptionDTO subscriptionDTO = subscriptionService.create(username, request.getVideoId(), request.getVideoLink());
            return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Не удалось оформить подписку: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyActiveSubscriptions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ошибка: пользователь не авторизован.");
        }

        try {
            String username = principal.getName();

            // Получаем список DTO подписок
            List<VideoSubscriptionDTO> mySubscriptions = subscriptionService.getActiveSubscriptions(username);

            return ResponseEntity.ok(mySubscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Не удалось получить список подписок: " + e.getMessage());
        }
    }
}
