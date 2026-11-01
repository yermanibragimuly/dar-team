package com.example.demo.controller.shop;

import com.example.demo.dto.shop.BranchStockItemDTO;
import com.example.demo.dto.shop.UpdateStockRequest;
import com.example.demo.entity.users.Role;
import com.example.demo.entity.users.User;
import com.example.demo.repository.roles.UserRepository;
import com.example.demo.service.shop.BranchStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class BranchStockController {

    private final BranchStockService branchStockService;
    private final UserRepository userRepository;

    // Публичный эндпоинт: остатки конкретного товара по филиалам для карточки на сайте
    @GetMapping({"/api/v1/shop/products/{productId}/stocks", "/v1/shop/products/{productId}/stocks"})
    public ResponseEntity<List<BranchStockItemDTO>> getProductStocks(@PathVariable Long productId) {
        return ResponseEntity.ok(branchStockService.getProductStocksPublic(productId));
    }

    // Реестр остатков зала (Менеджер зала видит только свой филиал, Админ — любой)
    @GetMapping({"/api/admin/v1/branches/{branchId}/stocks", "/admin/v1/branches/{branchId}/stocks"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> getBranchStocks(@PathVariable Long branchId, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isSuperAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);

        Long targetBranchId = branchId;
        if (!isSuperAdmin) {
            targetBranchId = currentUser.getBranchId();
            if (targetBranchId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "За вашим аккаунтом не закреплен филиал"));
            }
        } else if (targetBranchId == null || targetBranchId <= 0) {
            targetBranchId = 1L;
        }

        return ResponseEntity.ok(branchStockService.getStocksByBranch(targetBranchId));
    }

    // Сохранение остатков менеджером
    @PutMapping({"/api/admin/v1/branches/{branchId}/stocks", "/admin/v1/branches/{branchId}/stocks"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<?> updateBranchStocks(
            @PathVariable Long branchId,
            @RequestBody List<UpdateStockRequest> requests,
            Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isSuperAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);

        Long targetBranchId = branchId;
        if (!isSuperAdmin) {
            targetBranchId = currentUser.getBranchId();
            if (targetBranchId == null || !Objects.equals(targetBranchId, branchId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Нет доступа к складу другого филиала"));
            }
        }

        branchStockService.updateStock(targetBranchId, requests);
        return ResponseEntity.ok(Map.of("success", true, "message", "Остатки филиала успешно сохранены"));
    }
}