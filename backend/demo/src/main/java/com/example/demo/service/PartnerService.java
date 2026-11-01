package com.example.demo.service;

import com.example.demo.dto.PartnerDTO;
import com.example.demo.entity.Partner;
import com.example.demo.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final MinioService minioService;

    public List<PartnerDTO> getAll() {
        return partnerRepository.findAllByOrderByPositionIndexAsc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public PartnerDTO create(PartnerDTO dto) {
        Partner partner = Partner.builder()
                .name(dto.getName())
                .logoUrl(dto.getLogoUrl())
                .websiteUrl(dto.getWebsiteUrl())
                .positionIndex(dto.getPositionIndex() != null ? dto.getPositionIndex() : 0)
                .build();

        return mapToDTO(partnerRepository.save(partner));
    }

    @Transactional
    public PartnerDTO update(Long id, PartnerDTO dto) {
        Partner existing = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер не найден: " + id));

        existing.setName(dto.getName());
        existing.setWebsiteUrl(dto.getWebsiteUrl());

        // Если логотип изменился, удаляем старый из MinIO
        if (dto.getLogoUrl() != null && !dto.getLogoUrl().equals(existing.getLogoUrl())) {
            minioService.deleteFileByUrl(existing.getLogoUrl());
            existing.setLogoUrl(dto.getLogoUrl());
        }

        if (dto.getPositionIndex() != null) {
            existing.setPositionIndex(dto.getPositionIndex());
        }

        return mapToDTO(partnerRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер не найден: " + id));

        // Удаляем логотип из MinIO
        if (partner.getLogoUrl() != null && !partner.getLogoUrl().isBlank()) {
            minioService.deleteFileByUrl(partner.getLogoUrl());
        }

        partnerRepository.delete(partner);
    }

    private PartnerDTO mapToDTO(Partner p) {
        return PartnerDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .logoUrl(p.getLogoUrl())
                .websiteUrl(p.getWebsiteUrl())
                .positionIndex(p.getPositionIndex())
                .build();
    }
}