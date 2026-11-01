package com.example.demo.service.shop;

import com.example.demo.dto.shop.ProductDTO;
import com.example.demo.dto.shop.ProductImageDTO;
import com.example.demo.dto.shop.ProductVariantDTO;
import com.example.demo.entity.shop.ProductImage;
import com.example.demo.entity.shop.ProductVariant;
import com.example.demo.entity.shop.ShopProduct;
import com.example.demo.repository.shop.ShopProductRepository;
import com.example.demo.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopProductService {

    private final ShopProductRepository repository;
    private final MinioService minioService;
    private final BranchStockService branchStockService;

    public List<ProductDTO> getAllAdmin() {
        return repository.findAllByOrderByPositionIndexAsc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ProductDTO> getAllActive() {
        return repository.findAllByIsActiveTrueOrderByPositionIndexAsc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ProductDTO getById(Long id) {
        ShopProduct product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return mapToDTO(product);
    }

    @Transactional
    public ProductDTO create(ProductDTO dto) {
        ShopProduct product = new ShopProduct();
        updateEntityFromDTO(product, dto);
        ShopProduct saved = repository.save(product);

        // Автоматически инициализируем записи остатков по всем залам сети
        branchStockService.initStocksForProduct(saved);

        return mapToDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        ShopProduct existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        updateEntityFromDTO(existing, dto);
        ShopProduct saved = repository.save(existing);

        // Гарантируем остатки для всех обновленных/добавленных размеров
        branchStockService.initStocksForProduct(saved);

        return mapToDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        ShopProduct product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (product.getImages() != null) {
            for (ProductImage img : product.getImages()) {
                if (img.getImageUrl() != null && !img.getImageUrl().isEmpty()) {
                    minioService.deleteFileByUrl(img.getImageUrl());
                }
            }
        }
        repository.delete(product);
    }

    @Transactional
    public void updateStatus(Long id, boolean active) {
        ShopProduct product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setIsActive(active);
        repository.save(product);
    }

    @Transactional
    public void updateOrder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            repository.updatePositionIndex(ids.get(i), i);
        }
    }

    private void updateEntityFromDTO(ShopProduct entity, ProductDTO dto) {
        entity.setName(dto.getName());
        entity.setSku(dto.getSku());
        entity.setCategory(dto.getCategory());
        entity.setCollection(dto.getCollection());
        entity.setRetailPrice(dto.getRetailPrice());
        entity.setProPrice(dto.getProPrice());
        entity.setCostPrice(dto.getCostPrice());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        entity.setIsProDiscount(dto.getIsProDiscount() != null ? dto.getIsProDiscount() : true);

        if (entity.getImages() == null) {
            entity.setImages(new ArrayList<>());
        } else {
            entity.getImages().clear();
        }
        if (dto.getImages() != null) {
            for (ProductImageDTO imgDto : dto.getImages()) {
                ProductImage img = ProductImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .isMain(imgDto.getIsMain() != null ? imgDto.getIsMain() : false)
                        .product(entity)
                        .build();
                entity.getImages().add(img);
            }
        }

        if (entity.getVariants() == null) {
            entity.setVariants(new ArrayList<>());
        } else {
            entity.getVariants().clear();
        }
        if (dto.getVariants() != null) {
            for (ProductVariantDTO vDto : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .size(vDto.getSize())
                        .stockQuantity(vDto.getStockQuantity() != null ? vDto.getStockQuantity() : 0)
                        .product(entity)
                        .build();
                entity.getVariants().add(variant);
            }
        }
    }

    private ProductDTO mapToDTO(ShopProduct entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSku(entity.getSku());
        dto.setCategory(entity.getCategory());
        dto.setCollection(entity.getCollection());
        dto.setRetailPrice(entity.getRetailPrice());
        dto.setProPrice(entity.getProPrice());
        dto.setCostPrice(entity.getCostPrice());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setIsProDiscount(entity.getIsProDiscount());
        dto.setPositionIndex(entity.getPositionIndex());

        if (entity.getImages() != null) {
            dto.setImages(entity.getImages().stream().map(img -> {
                ProductImageDTO imgDto = new ProductImageDTO();
                imgDto.setId(img.getId());
                imgDto.setImageUrl(img.getImageUrl());
                imgDto.setIsMain(img.getIsMain());
                return imgDto;
            }).toList());
        }

        if (entity.getVariants() != null) {
            dto.setVariants(entity.getVariants().stream().map(v -> {
                ProductVariantDTO vDto = new ProductVariantDTO();
                vDto.setId(v.getId());
                vDto.setSize(v.getSize());
                vDto.setStockQuantity(v.getStockQuantity());
                return vDto;
            }).toList());

            int total = entity.getVariants().stream()
                    .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                    .sum();
            dto.setTotalStock(total);
        } else {
            dto.setTotalStock(0);
        }

        return dto;
    }
}