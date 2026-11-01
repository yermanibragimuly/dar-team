package com.example.demo.Initializer;

import com.example.demo.entity.coach.ScheduleCoach;
import com.example.demo.repository.coache.ScheduleCoachRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduleInitializer implements CommandLineRunner {
    private final ScheduleCoachRepository scheduleCoachRepository;

    public ScheduleInitializer(ScheduleCoachRepository scheduleCoachRepository) {
        this.scheduleCoachRepository = scheduleCoachRepository;
    }

    @Override
    public void run (String... args) {
        if (scheduleCoachRepository.count() == 0) {
            List<ScheduleCoach> schedules = List.of(
                    ScheduleCoach.builder().day("Понедельник").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Вторник").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Среда").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Четверг").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Пятница").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Суббота").time("").activity("").coach("").build(),
                    ScheduleCoach.builder().day("Воскресенье").time("").activity("").coach("").build()
            );
            scheduleCoachRepository.saveAll(schedules);
        }
    }
}
