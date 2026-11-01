package com.example.demo.entity.shop;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shop_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    private String category;
    private String collection;

    @Column(name = "retail_price", nullable = false)
    private Integer retailPrice;

    @Column(name = "pro_price")
    private Integer proPrice;

    @Column(name = "cost_price")
    private Integer costPrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_pro_discount")
    @Builder.Default
    private Boolean isProDiscount = true;

    @Column(name = "position_index")
    @Builder.Default
    private Integer positionIndex = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
}