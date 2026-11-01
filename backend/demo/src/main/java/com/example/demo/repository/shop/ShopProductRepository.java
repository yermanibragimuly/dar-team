package com.example.demo.repository.shop;

import com.example.demo.entity.shop.ShopProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

    List<ShopProduct> findAllByOrderByPositionIndexAsc();

    List<ShopProduct> findAllByIsActiveTrueOrderByPositionIndexAsc();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ShopProduct p SET p.positionIndex = :index WHERE p.id = :id")
    void updatePositionIndex(@Param("id") Long id, @Param("index") int index);
}