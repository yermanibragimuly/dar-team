package com.example.demo.repository.fighters;


import com.example.demo.entity.fighter.Fighters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FightersRepository extends JpaRepository<Fighters, Long> {

    // Метод для получения списка по порядку
    List<Fighters> findAllByOrderByPositionIndexAsc();

    // Специальный запрос для обновления только индекса (быстрее, чем save)
    @Modifying
    @Query("UPDATE Fighters f SET f.positionIndex = :index WHERE f.id = :id")
    void updatePositionIndex(@Param("id") Long id, @Param("index") Integer index);
}