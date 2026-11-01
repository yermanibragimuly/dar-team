package com.example.demo.service.fighters;

import com.example.demo.dto.fighter.AchievementDTO;
import com.example.demo.dto.fighter.FighterDTO;
import com.example.demo.entity.fighter.Achievement;
import com.example.demo.entity.fighter.Fighters;
import com.example.demo.repository.fighters.AchievementRepository;
import com.example.demo.repository.fighters.FightersRepository;
import com.example.demo.service.MinioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FightersService {

    private final FightersRepository fighterRepository;
    private final AchievementRepository achievementRepository;
    private final MinioService minioService;

    public FightersService(FightersRepository fighterRepository,
                           AchievementRepository achievementRepository, MinioService minioService) {
        this.fighterRepository = fighterRepository;
        this.achievementRepository = achievementRepository;
        this.minioService = minioService;
    }

    // CREATE
    public FighterDTO createFighter(Fighters fighter) {

        if (fighter.getAchievements() != null) {
            for (Achievement a : fighter.getAchievements()) {
                a.setFighter(fighter);
            }
        }

        Fighters saved = fighterRepository.save(fighter);
        return mapToDTO(saved);
    }

    // UPDATE
    public FighterDTO updateFighter(Long id, Fighters updated) {
        Fighters existing = fighterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        existing.setName(updated.getName());
        existing.setNickname(updated.getNickname());
        existing.setBirthplace(updated.getBirthplace());
        existing.setWeightClass(updated.getWeightClass());
        existing.setRecord(updated.getRecord());
        existing.setTko(updated.getTko());
        existing.setSolution(updated.getSolution());
        existing.setSubmissive(updated.getSubmissive());
        existing.setOther(updated.getOther());
        existing.setCountry(updated.getCountry());
        existing.setSport(updated.getSport());
        existing.setRank(updated.getRank());
        existing.setInstagram(updated.getInstagram());
        existing.setImg(updated.getImg());

        existing.getAchievements().clear();

        if (updated.getAchievements() != null) {
            for (Achievement a : updated.getAchievements()) {
                a.setFighter(existing);
                existing.getAchievements().add(a);
            }
        }

        Fighters saved = fighterRepository.save(existing);
        return mapToDTO(saved);
    }

    // DELETE
    public FighterDTO deleteFighter(Long id) {
        Fighters fighter = fighterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        // 1. Удаляем картинку из MinIO
        minioService.deleteFileByUrl(fighter.getImg());

        fighterRepository.delete(fighter);
        return mapToDTO(fighter);
    }

    public FighterDTO deleteFighterImage(Long id) {
        Fighters fighter = fighterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        // 1. Физически удаляем файл из хранилища
        if (fighter.getImg() != null && !fighter.getImg().isEmpty()) {
            minioService.deleteFileByUrl(fighter.getImg());
        }

        // 2. Обнуляем ссылку в БД
        fighter.setImg(null);
        Fighters saved = fighterRepository.save(fighter);

        return mapToDTO(saved);
    }

    public AchievementDTO deleteAchievement(Long fighterId, Long achievementId) {

        Fighters fighter = fighterRepository.findById(fighterId)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        Achievement achievement = fighter.getAchievements().stream()
                .filter(a -> a.getId().equals(achievementId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        fighter.getAchievements().remove(achievement);

        fighterRepository.save(fighter);

        return mapAchievementToDTO(achievement);
    }


    // ADD ACHIEVEMENT
    public AchievementDTO addAchievement(Long fighterId, String title) {
        Fighters fighter = fighterRepository.findById(fighterId)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));

        Achievement a = new Achievement();
        a.setTitle(title);
        a.setFighter(fighter);

        fighter.getAchievements().add(a);

        fighterRepository.save(fighter);

        return mapAchievementToDTO(a);
    }

    // GET
    public FighterDTO getFighter(Long id) {
        Fighters fighter = fighterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fighter not found"));
        return mapToDTO(fighter);
    }

    public List<FighterDTO> getAllFighters() {
        return fighterRepository.findAllByOrderByPositionIndexAsc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //ДЛЯ ПЕРЕСОРТИРОВКИ
    @Transactional // Очень важно для UPDATE запросов
    public void updateOrder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            // Берем ID из списка и присваиваем ему индекс i (0, 1, 2...)
            fighterRepository.updatePositionIndex(ids.get(i), i);
        }
    }

    // ----- MAPPERS -----

    private FighterDTO mapToDTO(Fighters fighter) {
        FighterDTO dto = new FighterDTO();

        dto.setId(fighter.getId());
        dto.setPositionIndex(fighter.getPositionIndex());
        dto.setName(fighter.getName());
        dto.setNickname(fighter.getNickname());
        dto.setBirthplace(fighter.getBirthplace());
        dto.setWeightClass(fighter.getWeightClass());
        dto.setRecord(fighter.getRecord());
        dto.setTko(fighter.getTko());
        dto.setSolution(fighter.getSolution());
        dto.setSubmissive(fighter.getSubmissive());
        dto.setOther(fighter.getOther());
        dto.setCountry(fighter.getCountry());
        dto.setSport(fighter.getSport());
        dto.setRank(fighter.getRank());
        dto.setInstagram(fighter.getInstagram());
        dto.setImg(fighter.getImg());

        dto.setAchievements(
                fighter.getAchievements().stream()
                        .map(this::mapAchievementToDTO)
                        .toList()
        );

        return dto;
    }

    private AchievementDTO mapAchievementToDTO(Achievement achievement) {
        AchievementDTO dto = new AchievementDTO();
        dto.setId(achievement.getId());
        dto.setTitle(achievement.getTitle());
        return dto;
    }
}
