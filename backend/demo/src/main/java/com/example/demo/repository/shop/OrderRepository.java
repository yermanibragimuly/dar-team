package com.example.demo.repository.shop;

import com.example.demo.entity.shop.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findAllByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<Order> findAllByOrderByCreatedAtDesc();

    // Для автоматической отмены просроченной брони через 24 часа
    List<Order> findAllByOrderStatusAndReservedUntilBefore(String orderStatus, LocalDateTime now);
}