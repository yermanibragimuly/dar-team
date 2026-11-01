package com.example.demo.controller.fighters;

import com.example.demo.entity.fighter.Achievement;
import com.example.demo.service.fighters.AchievementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/v1/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping
    public Achievement createAchievement(@RequestParam Long fighterId,
                                         @RequestParam String title) {
        return achievementService.createAchievement(fighterId, title);
    }
}

