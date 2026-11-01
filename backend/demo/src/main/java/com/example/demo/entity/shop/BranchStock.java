package com.example.demo.entity.shop;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "branch_stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"branch_id", "product_variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0; // Физическое наличие

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0; // В резерве под заказы

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Доступно для продажи (Физика минус Резерв)
    public int getAvailableQuantity() {
        return Math.max(0, quantity - reservedQuantity);
    }
}