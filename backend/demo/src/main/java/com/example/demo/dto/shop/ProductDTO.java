package com.example.demo.dto.shop;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private String collection;
    private Integer retailPrice;
    private Integer proPrice;
    private Integer costPrice;
    private String description;
    private Boolean isActive;
    private Boolean isProDiscount;
    private Integer positionIndex;
    private Integer totalStock;
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
}