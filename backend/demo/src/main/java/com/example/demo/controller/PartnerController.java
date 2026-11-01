package com.example.demo.controller;

import com.example.demo.dto.PartnerDTO;
import com.example.demo.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    // Публичный запрос для главной страницы сайта
    @GetMapping({"/api/v1/partners", "/v1/partners"})
    public ResponseEntity<List<PartnerDTO>> getPublicPartners() {
        return ResponseEntity.ok(partnerService.getAll());
    }

    // Реестр в панели управления
    @GetMapping({"/api/admin/v1/partners", "/admin/v1/partners"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<PartnerDTO>> getAdminPartners() {
        return ResponseEntity.ok(partnerService.getAll());
    }

    @PostMapping({"/api/admin/v1/partners", "/admin/v1/partners"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<PartnerDTO> createPartner(@RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.create(dto));
    }

    @PutMapping({"/api/admin/v1/partners/{id}", "/admin/v1/partners/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<PartnerDTO> updatePartner(@PathVariable Long id, @RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.update(id, dto));
    }

    @DeleteMapping({"/api/admin/v1/partners/{id}", "/admin/v1/partners/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}