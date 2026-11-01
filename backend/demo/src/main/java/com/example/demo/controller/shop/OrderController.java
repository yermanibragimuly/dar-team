package com.example.demo.controller.shop;

import com.example.demo.dto.shop.CreateOrderRequest;
import com.example.demo.dto.shop.OrderDTO;
import com.example.demo.entity.shop.Branch;
import com.example.demo.entity.shop.Order;
import com.example.demo.entity.users.Role;
import com.example.demo.entity.users.User;
import com.example.demo.repository.roles.UserRepository;
import com.example.demo.repository.shop.BranchRepository;
import com.example.demo.repository.shop.OrderRepository;
import com.example.demo.service.shop.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    @GetMapping({"/api/v1/shop/branches", "/v1/shop/branches"})
    public ResponseEntity<List<Branch>> getBranches() {
        return ResponseEntity.ok(branchRepository.findAll());
    }

    @PostMapping({"/api/v1/shop/orders", "/v1/shop/orders"})
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) {
        try {
            Order order = orderService.createOrder(req);
            String redirectUrl;
            if ("at_branch".equals(order.getPaymentMethod())) {
                redirectUrl = "/checkout/success?order=" + order.getOrderNumber() + "&mode=branch";
            } else {
                redirectUrl = "/mock-payment/" + order.getId();
            }
            return ResponseEntity.ok(Map.of(
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber(),
                    "redirectUrl", redirectUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping({"/api/v1/shop/mock-payment/callback", "/v1/shop/mock-payment/callback"})
    public ResponseEntity<?> mockPaymentCallback(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            String status = (String) body.get("status");
            orderService.handlePaymentCallback(orderId, status);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping({"/api/v1/shop/orders/{id}", "/v1/shop/orders/{id}"})
    public ResponseEntity<?> getOrderForPayment(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(orderService::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping({"/api/admin/v1/orders", "/admin/v1/orders"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> getAdminOrders(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String search,
            Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isSuperAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);

        List<Order> orders;
        if (!isSuperAdmin) {
            Long managerBranchId = currentUser.getBranchId();
            if (managerBranchId == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            orders = orderRepository.findAllByBranchIdOrderByCreatedAtDesc(managerBranchId);
        } else if (branchId != null && branchId > 0) {
            orders = orderRepository.findAllByBranchIdOrderByCreatedAtDesc(branchId);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream().filter(o ->
                    o.getOrderNumber().toLowerCase().contains(q) ||
                    o.getCustomerPhone().contains(q) ||
                    o.getCustomerName().toLowerCase().contains(q) ||
                    (o.getPickupCode() != null && o.getPickupCode().contains(q))
            ).toList();
        }

        List<OrderDTO> dtos = orders.stream().map(orderService::mapToDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // СБОРКА ЗАКАЗА И ОТПРАВКА EMAIL КЛИЕНТУ
    @PostMapping({"/api/admin/v1/orders/{id}/assemble", "/admin/v1/orders/{id}/assemble"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> assembleOrder(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN) && !Objects.equals(currentUser.getBranchId(), order.getBranchId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к заказам другого зала"));
        }

        try {
            Order assembled = orderService.assembleOrder(id);
            return ResponseEntity.ok(orderService.mapToDTO(assembled));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping({"/api/admin/v1/orders/{orderId}/items/{itemId}/change-size", "/admin/v1/orders/{orderId}/items/{itemId}/change-size"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> changeItemSize(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        Order order = orderRepository.findById(orderId).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN) && !Objects.equals(currentUser.getBranchId(), order.getBranchId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к заказам другого зала"));
        }

        try {
            String newSize = body.get("newSize");
            Order updated = orderService.changeOrderItemSize(orderId, itemId, newSize);
            return ResponseEntity.ok(orderService.mapToDTO(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping({"/api/admin/v1/orders/{id}/issue", "/admin/v1/orders/{id}/issue"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> issueOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {

        Order order = orderRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN) && !Objects.equals(currentUser.getBranchId(), order.getBranchId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к заказам другого филиала"));
        }

        try {
            String pickupCode = body.get("pickupCode");
            String paymentMethod = body.get("paymentMethod");
            Order issuedOrder = orderService.issueOrder(id, pickupCode, paymentMethod);
            return ResponseEntity.ok(orderService.mapToDTO(issuedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping({"/api/admin/v1/orders/{id}/refund", "/admin/v1/orders/{id}/refund"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> refundOrder(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN) && !Objects.equals(currentUser.getBranchId(), order.getBranchId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к заказам другого филиала"));
        }

        try {
            orderService.refundOrder(id);
            return orderRepository.findById(id)
                    .map(orderService::mapToDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping({"/api/admin/v1/orders/{id}/cancel", "/admin/v1/orders/{id}/cancel"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN) && !Objects.equals(currentUser.getBranchId(), order.getBranchId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к заказам другого филиала"));
        }

        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}