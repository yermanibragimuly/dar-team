package com.example.demo.service.video;

import com.example.demo.dto.video.VideoSubscriptionDTO;
import com.example.demo.entity.users.User;
import com.example.demo.entity.video.VideoSubscription;
import com.example.demo.entity.video.Videos;
import com.example.demo.repository.roles.UserRepository;
import com.example.demo.repository.video.VideoRepository;
import com.example.demo.repository.video.VideoSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoSubscriptionService {

    private final VideoSubscriptionRepository videoSubscriptionRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public VideoSubscriptionDTO create(String username, Long videoId, String videoLink) {
        LocalDateTime now = LocalDateTime.now();

        boolean alreadyHasAccess = videoSubscriptionRepository.hasActiveAccess(username, videoId, now);

        if (alreadyHasAccess) {
            throw new IllegalStateException("У вас уже есть активная подписка на это видео!");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        Videos video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("Видео не найдено"));

        LocalDateTime expiresAt = now.plusDays(30);

        VideoSubscription videoSubscription = VideoSubscription.builder()
                .user(user)
                .video(video)
                .activatedAt(now)
                .expiresAt(expiresAt)
                .build();

        VideoSubscription savedSubscription = videoSubscriptionRepository.save(videoSubscription);

        String link = (videoLink != null && !videoLink.isBlank())
                ? videoLink
                : (savedSubscription.getVideo() != null ? savedSubscription.getVideo().getYoutubeVideoId() : null);

        return VideoSubscriptionDTO.builder()
                .id(savedSubscription.getId())
                .userId(savedSubscription.getUser().getId())
                .videoId(savedSubscription.getVideo().getId())
                .videoLink(link)
                .activatedAt(savedSubscription.getActivatedAt())
                .expiresAt(savedSubscription.getExpiresAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<VideoSubscriptionDTO> getActiveSubscriptions(String username) {
        LocalDateTime now = LocalDateTime.now();

        List<VideoSubscription> subscriptions = videoSubscriptionRepository
                .findAllActiveSubscriptions(username, now);

        return subscriptions.stream()
                .map(sub -> {
                    String link = sub.getVideo() != null ? sub.getVideo().getYoutubeVideoId() : null;
                    return VideoSubscriptionDTO.builder()
                            .id(sub.getId())
                            .userId(sub.getUser().getId())
                            .videoId(sub.getVideo().getId())
                            .videoLink(link)
                            .activatedAt(sub.getActivatedAt())
                            .expiresAt(sub.getExpiresAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasActiveAccess(String username, Long videoId) {
        return videoSubscriptionRepository.hasActiveAccess(username, videoId, LocalDateTime.now());
    }
}