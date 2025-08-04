package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.models.Faculty;
import com.LDE.monFax_backend.requests.FacultyRequest;
import com.LDE.monFax_backend.services.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    public ResponseEntity<Faculty> createFaculty(@RequestBody FacultyRequest request) {
        Faculty savedFaculty = facultyService.createFaculty(request);
        return ResponseEntity.ok(savedFaculty);
    }

    @GetMapping
    public ResponseEntity<List<Faculty>> getAllFaculties() {
        return ResponseEntity.ok(facultyService.getAllFaculties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Faculty> getFacultyById(@PathVariable Long id) {
        return facultyService.getFacultyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateFaculty(@PathVariable Long id, @RequestBody FacultyRequest request) throws Exception {
        try {
            facultyService.updateFaculty(id, request);
            return ResponseEntity.ok("Departement mise à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        boolean deleted = facultyService.deleteFaculty(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}