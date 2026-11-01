package com.example.demo.dto.shop;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;
    private String size;
    private Integer stockQuantity;
}