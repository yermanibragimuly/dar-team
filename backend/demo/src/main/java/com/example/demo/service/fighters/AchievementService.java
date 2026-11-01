package com.example.demo.service.fighters;

import com.example.demo.entity.fighter.Achievement;
import com.example.demo.entity.fighter.Fighters;
import com.example.demo.repository.fighters.AchievementRepository;
import com.example.demo.repository.fighters.FightersRepository;
import org.springframework.stereotype.Service;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final FightersRepository fighterRepository;

    public AchievementService(AchievementRepository achievementRepository,
                              FightersRepository fighterRepository) {
        this.achievementRepository = achievementRepository;
        this.fighterRepository = fighterRepository;
    }

    public Achievement createAchievement(Long fighterId, String title) {
        Fighters fighter = fighterRepository.findById(fighterId)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        Achievement achievement = new Achievement();
        achievement.setTitle(title);
        achievement.setFighter(fighter);

        return achievementRepository.save(achievement);
    }
}

