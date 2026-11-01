package com.example.demo.repository.shop;

import com.example.demo.entity.shop.BranchStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchStockRepository extends JpaRepository<BranchStock, Long> {

    Optional<BranchStock> findByBranchIdAndVariantId(Long branchId, Long variantId);

    List<BranchStock> findAllByBranchId(Long branchId);

    @Query("SELECT bs FROM BranchStock bs JOIN FETCH bs.branch JOIN FETCH bs.variant v JOIN FETCH v.product WHERE v.product.id = :productId")
    List<BranchStock> findAllByProductId(@Param("productId") Long productId);

    void deleteAllByVariantId(Long variantId);
}