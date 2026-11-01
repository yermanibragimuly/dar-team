package com.example.demo.entity.coach;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCoach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String day;

    private String time;
    private String activity;
    private String coach;
}
