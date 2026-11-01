package com.example.demo.dto.video;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSubscriptionDTO {
    private Long id;
    private Long userId;
    private Long videoId;
    private String videoLink;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;
}
