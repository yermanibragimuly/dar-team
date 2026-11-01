package com.example.demo.dto.shop;

import lombok.Data;

@Data
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private Boolean isMain;
}