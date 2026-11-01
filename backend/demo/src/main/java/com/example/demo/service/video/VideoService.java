package com.example.demo.service.video;

import com.example.demo.dto.video.VideoDTO;
import com.example.demo.entity.video.Videos;
import com.example.demo.repository.video.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoDTO getVideoById(Long id) {
        Videos video = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Видео с ID " + id + " не найдено."));
        return mapToDTO(video);
    }

    public List<VideoDTO> getAllVideos() {
        return videoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<VideoDTO> getPublicVideos() {
        return videoRepository.findByIsPremiumFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public VideoDTO createVideo(VideoDTO dto) {
        if (videoRepository.existsByYoutubeVideoId(dto.getYoutubeVideoId())) {
            throw new IllegalArgumentException("Видео с таким YouTube ID уже добавлено на сайт.");
        }

        Videos video = Videos.builder()
                .title(dto.getTitle())
                .youtubeVideoId(dto.getYoutubeVideoId())
                .build();

        Videos savedVideo = videoRepository.save(video);
        return mapToDTO(savedVideo);
    }

    public VideoDTO updateVideo(Long id, VideoDTO dto) {
        Videos video = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Видео для обновления не найдено."));

        video.setTitle(dto.getTitle());
        video.setYoutubeVideoId(dto.getYoutubeVideoId());

        Videos updatedVideo = videoRepository.save(video);
        return mapToDTO(updatedVideo);
    }

    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new EntityNotFoundException("Невозможно удалить: видео с ID " + id + " не существует.");
        }
        videoRepository.deleteById(id);
    }



    private VideoDTO mapToDTO(Videos video) {
        return VideoDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .youtubeVideoId(video.getYoutubeVideoId())
                .isPremium(video.isPremium())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
