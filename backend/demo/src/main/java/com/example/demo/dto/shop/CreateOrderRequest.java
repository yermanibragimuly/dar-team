package com.example.demo.dto.shop;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long branchId;
    private String deliveryType;
    private String paymentMethod;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private String size;
        private Integer quantity;
    }
}