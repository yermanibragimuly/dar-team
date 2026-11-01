package com.example.demo.service.shop;

import com.example.demo.dto.shop.CreateOrderRequest;
import com.example.demo.dto.shop.OrderDTO;
import com.example.demo.entity.shop.*;
import com.example.demo.repository.shop.BranchRepository;
import com.example.demo.repository.shop.BranchStockRepository;
import com.example.demo.repository.shop.OrderRepository;
import com.example.demo.repository.shop.ShopProductRepository;
import com.example.demo.service.Email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShopProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final BranchStockRepository branchStockRepository;
    private final BranchStockService branchStockService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Филиал не найден"));

        int totalAmount = 0;
        long orderSeq = orderRepository.count() + 1;
        String orderNumber = "DAR-" + (1000 + orderSeq);

        String pickupCode = String.valueOf(1000 + secureRandom.nextInt(9000));
        LocalDateTime reservedUntil = LocalDateTime.now().plusHours(24);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .customerEmail(req.getCustomerEmail())
                .branchId(branch.getId())
                .deliveryType(req.getDeliveryType() != null ? req.getDeliveryType() : "pickup")
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus("pending")
                .orderStatus("new")
                .pickupCode(pickupCode)
                .reservedUntil(reservedUntil)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        for (CreateOrderRequest.OrderItemRequest itemReq : req.getItems()) {
            ShopProduct product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getSize().equalsIgnoreCase(itemReq.getSize()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Размер не найден"));

            BranchStock stock = branchStockService.getOrCreateStock(branch, variant);

            if (stock.getAvailableQuantity() < itemReq.getQuantity()) {
                throw new IllegalStateException("Недостаточно товара " + product.getName() + " (" + itemReq.getSize() + "). Доступно: " + stock.getAvailableQuantity() + " шт.");
            }

            stock.setReservedQuantity(stock.getReservedQuantity() + itemReq.getQuantity());
            stock.setUpdatedAt(LocalDateTime.now());
            branchStockRepository.save(stock);

            int price = (product.getRetailPrice() != null) ? product.getRetailPrice() : 0;
            totalAmount += price * itemReq.getQuantity();

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .size(itemReq.getSize())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(price)
                    .build());
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    // СБОРКА ЗАКАЗА И ОТПРАВКА EMAIL
    @Transactional
    public Order assembleOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        order.setOrderStatus("ready_for_pickup");
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Отправка email клиенту
        Branch branch = branchRepository.findById(order.getBranchId()).orElse(null);
        String branchName = branch != null ? branch.getName() : "DAR GYM";
        String branchAddress = branch != null ? branch.getAddress() : "";

        if (order.getCustomerEmail() != null && !order.getCustomerEmail().isBlank()) {
            emailService.sendOrderReadyEmail(
                    order.getCustomerEmail(),
                    order.getCustomerName(),
                    order.getOrderNumber(),
                    order.getPickupCode(),
                    branchName,
                    branchAddress
            );
        }

        return saved;
    }

    @Transactional
    public Order changeOrderItemSize(Long orderId, Long orderItemId, String newSize) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        if ("completed".equalsIgnoreCase(order.getOrderStatus()) || "cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Нельзя изменить выданный или отмененный заказ");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Позиция заказа не найдена"));

        if (item.getSize().equalsIgnoreCase(newSize)) return order;

        Branch branch = branchRepository.findById(order.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Филиал заказа не найден"));

        ProductVariant oldVariant = getVariant(item.getProductId(), item.getSize());
        ProductVariant newVariant = getVariant(item.getProductId(), newSize);

        BranchStock oldStock = branchStockService.getOrCreateStock(branch, oldVariant);
        BranchStock newStock = branchStockService.getOrCreateStock(branch, newVariant);

        if (newStock.getAvailableQuantity() < item.getQuantity()) {
            throw new IllegalStateException("Размера " + newSize + " нет в наличии! Доступно: " + newStock.getAvailableQuantity() + " шт.");
        }

        oldStock.setReservedQuantity(Math.max(0, oldStock.getReservedQuantity() - item.getQuantity()));
        newStock.setReservedQuantity(newStock.getReservedQuantity() + item.getQuantity());
        oldStock.setUpdatedAt(LocalDateTime.now());
        newStock.setUpdatedAt(LocalDateTime.now());

        branchStockRepository.save(oldStock);
        branchStockRepository.save(newStock);

        item.setSize(newSize);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public void handlePaymentCallback(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));

        if ("paid".equalsIgnoreCase(status)) {
            order.setPaymentStatus("paid");
        } else {
            releaseStockForOrder(order);
            order.setPaymentStatus("failed");
            order.setOrderStatus("cancelled");
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void refundOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));

        if ("refunded".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new IllegalStateException("По данному заказу возврат уже произведён");
        }

        releaseStockForOrder(order);

        order.setPaymentStatus("refunded");
        order.setOrderStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void releaseStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                ProductVariant variant = getVariant(item.getProductId(), item.getSize());
                branchStockRepository.findByBranchIdAndVariantId(order.getBranchId(), variant.getId())
                        .ifPresent(stock -> {
                            if ("completed".equalsIgnoreCase(order.getOrderStatus())) {
                                stock.setQuantity(stock.getQuantity() + item.getQuantity());
                            } else {
                                stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - item.getQuantity()));
                            }
                            stock.setUpdatedAt(LocalDateTime.now());
                            branchStockRepository.save(stock);
                        });
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    public Order issueOrder(Long orderId, String inputCode, String actualPaymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        if (order.getPickupCode() != null && !order.getPickupCode().trim().equals(inputCode != null ? inputCode.trim() : "")) {
            throw new IllegalArgumentException("Неверный код выдачи!");
        }

        if ("completed".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Заказ уже выдан");
        }

        for (OrderItem item : order.getItems()) {
            ProductVariant variant = getVariant(item.getProductId(), item.getSize());
            BranchStock stock = branchStockRepository.findByBranchIdAndVariantId(order.getBranchId(), variant.getId())
                    .orElseThrow(() -> new IllegalStateException("Остаток товара не найден"));

            stock.setQuantity(Math.max(0, stock.getQuantity() - item.getQuantity()));
            stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - item.getQuantity()));
            stock.setUpdatedAt(LocalDateTime.now());
            branchStockRepository.save(stock);
        }

        order.setOrderStatus("completed");
        order.setPaymentStatus("paid");
        if (actualPaymentMethod != null && !actualPaymentMethod.isBlank()) {
            order.setPaymentMethod(actualPaymentMethod);
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        if ("cancelled".equalsIgnoreCase(order.getOrderStatus()) || "completed".equalsIgnoreCase(order.getOrderStatus())) {
            return;
        }

        releaseStockForOrder(order);

        order.setOrderStatus("cancelled");
        order.setPaymentStatus("failed");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private ProductVariant getVariant(Long productId, String size) {
        ShopProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        return product.getVariants().stream()
                .filter(v -> v.getSize().equalsIgnoreCase(size))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Размер " + size + " не найден"));
    }

    public OrderDTO mapToDTO(Order order) {
        Branch branch = branchRepository.findById(order.getBranchId()).orElse(null);
        String branchName = branch != null ? branch.getName() : "Филиал #" + order.getBranchId();
        String branchAddress = branch != null ? branch.getAddress() : "";

        List<OrderDTO.OrderItemDTO> itemsDTO = order.getItems().stream()
                .map(i -> OrderDTO.OrderItemDTO.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .size(i.getSize())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .branchId(order.getBranchId())
                .branchName(branchName)
                .branchAddress(branchAddress)
                .deliveryType(order.getDeliveryType())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .pickupCode(order.getPickupCode())
                .reservedUntil(order.getReservedUntil()) // <-- ПЕРЕДАЕМ ДАТУ ОКОНЧАНИЯ БРОНИ
                .createdAt(order.getCreatedAt())
                .items(itemsDTO)
                .build();
    }
}