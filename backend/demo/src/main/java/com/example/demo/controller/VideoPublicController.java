package com.example.demo.controller;

import com.example.demo.dto.video.VideoDTO;
import com.example.demo.service.video.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/videos")
public class VideoPublicController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoDTO>> getPublicVideos() {
        return ResponseEntity.ok(videoService.getPublicVideos());
    }

}
