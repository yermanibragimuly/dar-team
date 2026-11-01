package com.example.demo.controller.shop;

import com.example.demo.dto.shop.ProductDTO;
import com.example.demo.service.shop.ShopProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShopProductController {

    private final ShopProductService productService;

    // Публичный эндпоинт для витрины: идет через /api/, поэтому Caddy направляет его в бэкенд
    @GetMapping("/api/v1/shop/products")
    public ResponseEntity<List<ProductDTO>> getPublicProducts() {
        return ResponseEntity.ok(productService.getAllActive());
    }

    // Реестр для админки
    @GetMapping("/admin/v1/shop/products")
    public ResponseEntity<List<ProductDTO>> getAdminProducts() {
        return ResponseEntity.ok(productService.getAllAdmin());
    }

    @GetMapping("/admin/v1/shop/products/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping("/admin/v1/shop/products")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.create(dto));
    }

    @PutMapping("/admin/v1/shop/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @PatchMapping("/admin/v1/shop/products/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id, @RequestParam boolean active) {
        productService.updateStatus(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/v1/shop/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}