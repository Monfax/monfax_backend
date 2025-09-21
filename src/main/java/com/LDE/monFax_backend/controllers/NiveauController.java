package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.models.Niveau;
import com.LDE.monFax_backend.services.NiveauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController {

    private final NiveauService niveauService;

    @GetMapping
    public List<Niveau> getAllNiveaux() {
        return niveauService.getAllNiveaux();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Niveau> getNiveauById(@PathVariable Long id) {
        return niveauService.getNiveauById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Niveau createNiveau(@RequestBody Niveau niveau) {
        return niveauService.createNiveau(niveau);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Niveau> updateNiveau(@PathVariable Long id, @RequestBody Niveau niveau) {
        try {
            return ResponseEntity.ok(niveauService.updateNiveau(id, niveau));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNiveau(@PathVariable Long id) {
        niveauService.deleteNiveau(id);
        return ResponseEntity.noContent().build();
    }
}
