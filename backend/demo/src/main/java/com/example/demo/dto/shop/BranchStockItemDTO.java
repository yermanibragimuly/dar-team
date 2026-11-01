package com.example.demo.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchStockItemDTO {
    private Long branchId;             // <-- Теперь передаем ID филиала
    private String branchName;         // <-- Название филиала
    private Long variantId;
    private Long productId;
    private String productName;
    private String sku;
    private String size;
    private Integer quantity;          // Физическое наличие
    private Integer reservedQuantity;  // В брони под заказы
    private Integer availableQuantity; // Доступно к продаже (quantity - reservedQuantity)
}