package com.example.demo.entity.shop;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "delivery_type")
    @Builder.Default
    private String deliveryType = "pickup";

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; 

    @Column(name = "payment_status")
    @Builder.Default
    private String paymentStatus = "pending"; 

    @Column(name = "order_status")
    @Builder.Default
    private String orderStatus = "new"; 

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;
    
    // Поля для выдачи и резервирования
    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}