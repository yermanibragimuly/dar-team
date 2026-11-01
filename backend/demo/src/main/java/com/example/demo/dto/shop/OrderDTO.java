package com.example.demo.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String deliveryType;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private Integer totalAmount;
    private String pickupCode;
    private LocalDateTime reservedUntil; // <-- Передаем дату истечения 24 часов
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String size;
        private Integer quantity;
        private Integer unitPrice;
    }
}