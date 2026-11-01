package com.example.demo.service.shop;

import com.example.demo.dto.shop.BranchStockItemDTO;
import com.example.demo.dto.shop.UpdateStockRequest;
import com.example.demo.entity.shop.*;
import com.example.demo.repository.shop.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchStockService {

    private final BranchStockRepository branchStockRepository;
    private final BranchRepository branchRepository;
    private final ShopProductRepository productRepository;

    @Transactional
    public BranchStock getOrCreateStock(Branch branch, ProductVariant variant) {
        return branchStockRepository.findByBranchIdAndVariantId(branch.getId(), variant.getId())
                .orElseGet(() -> branchStockRepository.save(BranchStock.builder()
                        .branch(branch)
                        .variant(variant)
                        .quantity(0)
                        .reservedQuantity(0)
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    @Transactional
    public void initStocksForProduct(ShopProduct product) {
        List<Branch> branches = branchRepository.findAll();
        for (ProductVariant variant : product.getVariants()) {
            for (Branch branch : branches) {
                getOrCreateStock(branch, variant);
            }
        }
    }

    @Transactional
    public List<BranchStockItemDTO> getStocksByBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Филиал не найден"));

        List<ShopProduct> products = productRepository.findAll();
        List<BranchStockItemDTO> result = new ArrayList<>();

        for (ShopProduct p : products) {
            for (ProductVariant v : p.getVariants()) {
                BranchStock stock = getOrCreateStock(branch, v);
                result.add(BranchStockItemDTO.builder()
                        .branchId(branch.getId())
                        .branchName(branch.getName())
                        .variantId(v.getId())
                        .productId(p.getId())
                        .productName(p.getName())
                        .sku(p.getSku())
                        .size(v.getSize())
                        .quantity(stock.getQuantity())
                        .reservedQuantity(stock.getReservedQuantity())
                        .availableQuantity(stock.getAvailableQuantity())
                        .build());
            }
        }
        return result;
    }

    @Transactional
    public void updateStock(Long branchId, List<UpdateStockRequest> requests) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Филиал не найден"));

        for (UpdateStockRequest req : requests) {
            BranchStock stock = branchStockRepository.findByBranchIdAndVariantId(branch.getId(), req.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Остаток не найден"));

            stock.setQuantity(Math.max(0, req.getQuantity()));
            stock.setUpdatedAt(LocalDateTime.now());
            branchStockRepository.save(stock);
        }
    }

    @Transactional(readOnly = true)
    public List<BranchStockItemDTO> getProductStocksPublic(Long productId) {
        List<BranchStock> stocks = branchStockRepository.findAllByProductId(productId);
        return stocks.stream().map(s -> BranchStockItemDTO.builder()
                .branchId(s.getBranch().getId())
                .branchName(s.getBranch().getName())
                .variantId(s.getVariant().getId())
                .productId(productId)
                .productName(s.getVariant().getProduct().getName())
                .sku(s.getVariant().getProduct().getSku())
                .size(s.getVariant().getSize())
                .quantity(s.getQuantity())
                .reservedQuantity(s.getReservedQuantity())
                .availableQuantity(s.getAvailableQuantity())
                .build()).toList();
    }
}