package com.example.demo.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class NewsDTO {
    private Long id;
    private String title;
    private String titleEn;
    private String titleKz;
    private String content;
    private String contentEn;
    private String contentKz;
    private String img;
    private LocalDate date;
}
