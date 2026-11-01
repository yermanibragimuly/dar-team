package com.example.demo.repository.video;


import com.example.demo.entity.video.VideoSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoSubscriptionRepository extends JpaRepository<VideoSubscription,Long> {


    @Query("SELECT COUNT(s) > 0 FROM VideoSubscription s " +
            "WHERE s.user.username = :username " +
            "AND s.video.id = :videoId " +
            "AND s.expiresAt > :now")
    boolean hasActiveAccess(@Param("username") String username,
                            @Param("videoId") Long videoId,
                            @Param("now") LocalDateTime now);

    @Query("SELECT s FROM VideoSubscription s " +
            "WHERE s.user.username = :username " +
            "AND s.expiresAt > :now")
    List<VideoSubscription> findAllActiveSubscriptions(@Param("username") String username,
                                                       @Param("now") LocalDateTime now);
}
