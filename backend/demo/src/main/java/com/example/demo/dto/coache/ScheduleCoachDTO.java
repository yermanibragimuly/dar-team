package com.example.demo.dto.coache;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCoachDTO {
    private String day;
    private String time;
    private String activity;
    private String coach;
}
