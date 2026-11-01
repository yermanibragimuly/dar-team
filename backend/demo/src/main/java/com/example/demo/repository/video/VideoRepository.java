package com.example.demo.repository.video;

import com.example.demo.entity.video.Videos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository extends JpaRepository<Videos, Long> {

    boolean existsByYoutubeVideoId(String youtubeVideoId);

    // Публичные — только бесплатные
    List<Videos> findByIsPremiumFalse();
}
