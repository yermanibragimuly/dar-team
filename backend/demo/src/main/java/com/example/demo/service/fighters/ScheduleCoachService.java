package com.example.demo.service.fighters;

import com.example.demo.dto.coache.ScheduleCoachDTO;
import com.example.demo.entity.coach.ScheduleCoach;
import com.example.demo.repository.coache.ScheduleCoachRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleCoachService {
    private final ScheduleCoachRepository scheduleCoachRepository;

    private static final List<String> DAY_ORDER = List.of(
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"
    );

    public ScheduleCoachService (ScheduleCoachRepository scheduleCoachRepository) {
        this.scheduleCoachRepository = scheduleCoachRepository;
    }

    public List<ScheduleCoachDTO> getAll(){
        return scheduleCoachRepository.findAll()
                .stream()
                .sorted((a, b) ->
                        DAY_ORDER.indexOf(a.getDay()) - DAY_ORDER.indexOf(b.getDay())
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ScheduleCoachDTO update(String day, ScheduleCoachDTO request){
        ScheduleCoach scheduleCoach = scheduleCoachRepository.findByDay(day)
                .orElseThrow(()-> new RuntimeException("День не найден: " + day));

        scheduleCoach.setTime(request.getTime());
        scheduleCoach.setActivity(request.getActivity());
        scheduleCoach.setCoach(request.getCoach());

        ScheduleCoach saved = scheduleCoachRepository.save(scheduleCoach);
        return mapToDTO(saved);
    }

    private ScheduleCoachDTO mapToDTO(ScheduleCoach scheduleCoach){
        return ScheduleCoachDTO.builder()
                .day(scheduleCoach.getDay())
                .time(scheduleCoach.getTime())
                .activity(scheduleCoach.getActivity())
                .coach(scheduleCoach.getCoach())
                .build();
    }
}
