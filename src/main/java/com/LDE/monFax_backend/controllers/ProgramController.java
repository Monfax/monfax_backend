package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.Program;
import com.LDE.monFax_backend.requests.ProgramRequest;
import com.LDE.monFax_backend.services.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<?> createProgram(@RequestBody ProgramRequest request) {
        try {
            Program program = programService.createProgram(request);
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Program>> getAllPrograms() {
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Program> getProgramById(@PathVariable Long id) {
        return programService.getProgramById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProgram(@PathVariable Long id, @RequestBody ProgramRequest request) throws Exception {
        try {
            programService.updateProgram(id, request);
            return ResponseEntity.ok("Filiere mise à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        boolean deleted = programService.deleteProgram(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}