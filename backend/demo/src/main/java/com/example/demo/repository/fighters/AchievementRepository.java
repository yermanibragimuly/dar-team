package com.example.demo.repository.fighters;

import com.example.demo.entity.fighter.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement,Long> {

}
