package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.requests.SubjectRequest;
import com.LDE.monFax_backend.services.SubjectService;
import com.LDE.monFax_backend.services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody SubjectRequest request) {
        try {
            Subject subject = subjectService.createSubject(request);
            return ResponseEntity.ok(subject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubject() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return subjectService.getSubjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateSubject(@PathVariable Long id, @RequestBody SubjectRequest request) {
        try {
            subjectService.updateSubject(id, request);
            return ResponseEntity.ok("Matiere mise à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubject(@PathVariable Long id) {
        boolean deleted = subjectService.deleteSubject(id);
        return deleted ? ResponseEntity.ok().body("success to delete subject") : ResponseEntity.notFound().build();
    }
}