package com.example.demo.dto.video;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoDTO {
    private Long id;
    private String title;
    private String youtubeVideoId;
    private LocalDateTime createdAt;
    private Boolean isPremium;
}
