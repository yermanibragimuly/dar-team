package com.example.demo.controller.fighters;

import com.example.demo.dto.coache.ScheduleCoachDTO;
import com.example.demo.service.fighters.ScheduleCoachService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/v1/schedules")
public class ScheduleCoachController {
    private final ScheduleCoachService scheduleCoachService;

    public ScheduleCoachController(ScheduleCoachService scheduleCoachService) {
        this.scheduleCoachService = scheduleCoachService;
    }

    @GetMapping
    public List<ScheduleCoachDTO> getAll(){
        return scheduleCoachService.getAll();
    }

    @PutMapping("/{day}")
    public ScheduleCoachDTO update(@PathVariable String day, @RequestBody ScheduleCoachDTO request) {
        return scheduleCoachService.update(day, request);
    }
}
