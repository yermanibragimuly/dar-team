package com.example.demo.repository.coache;

import com.example.demo.entity.coach.ScheduleCoach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleCoachRepository extends JpaRepository<ScheduleCoach, Long> {
    Optional<ScheduleCoach> findByDay(String day);
}
