package com.example.demo.controller.fighters;

import com.example.demo.dto.fighter.AchievementDTO;
import com.example.demo.dto.fighter.FighterDTO;
import com.example.demo.entity.fighter.Fighters;
import com.example.demo.service.fighters.FightersService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/v1/fighters")
public class FightersController {

    private final FightersService fighterService;

    public FightersController(FightersService fighterService) {
        this.fighterService = fighterService;
    }

    @PostMapping
    public FighterDTO createFighter(@RequestBody Fighters fighter) {
        return fighterService.createFighter(fighter);
    }

    @PutMapping("/{id}")
    public FighterDTO updateFighter(@PathVariable Long id, @RequestBody Fighters fighter) {
        return fighterService.updateFighter(id, fighter);
    }

    @PostMapping("/reorder")
    public void reorderFighters(@RequestBody List<Long> ids) {
        fighterService.updateOrder(ids);
    }

    @DeleteMapping("/{id}")
    public FighterDTO deleteFighter(@PathVariable Long id) {
        return fighterService.deleteFighter(id);
    }

    @DeleteMapping("/{fighterId}/achievements/{achievementId}")
    public AchievementDTO deleteAchievement(
            @PathVariable Long fighterId,
            @PathVariable Long achievementId) {

        return fighterService.deleteAchievement(fighterId, achievementId);
    }

    @DeleteMapping("/{id}/image")
    public FighterDTO deleteFighterImage(@PathVariable Long id) {
        return fighterService.deleteFighterImage(id);
    }

    @PostMapping("/{id}/achievements")
    public AchievementDTO addAchievement(@PathVariable Long id, @RequestBody AchievementDTO dto) {
        return fighterService.addAchievement(id, dto.getTitle());
    }

    @GetMapping("/{id}")
    public FighterDTO getFighter(@PathVariable Long id) {
        return fighterService.getFighter(id);
    }

    @GetMapping
    public List<FighterDTO> getAllFighters() {
        return fighterService.getAllFighters();
    }
}
