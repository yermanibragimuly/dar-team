package com.example.demo.dto.shop;

import lombok.Data;

@Data
public class UpdateStockRequest {
    private Long variantId;
    private Integer quantity; // Новое физическое количество
}