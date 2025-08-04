package com.LDE.monFax_backend.controllers;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.LDE.monFax_backend.models.LectureCourse;
import com.LDE.monFax_backend.services.LectureCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Lecture Courses", description = "Gestion des supports de cours")
public class LectureCourseController {

    private final LectureCourseService courseService;

    @GetMapping
    @Operation(
            summary = "Liste tous les supports de cours word et pdf",
            description = "Retourne tous les supports de cours enregistrés",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des cours récupérée",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LectureCourse.class)))
            }
    )
    public ResponseEntity<List<LectureCourse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un support de  cours", description = "Retourne un support de cours spécifique à partir de son identifiant")
    public ResponseEntity<LectureCourse> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Créer un support de  cours", description = "Crée un support de cours pour une matiere specifique  avec upload de fichier associé")
    public ResponseEntity<String> createCourse(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        try {
            LectureCourse course = courseService.createCourse(title, description, price, subjectId, file);
            return ResponseEntity.ok("support de cours cree avec success");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    @Operation(summary = "Met à jour un support cours magistral", description = "Met à jour un support cours magistral avec possibilité de changer le fichier")
    @ApiResponse(responseCode = "200", description = "Cours mis à jour", content = @Content(schema = @Schema(implementation = LectureCourse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<LectureCourse> updateLectureCourse(
            @Parameter(description = "ID du cours à mettre à jour", required = true) @PathVariable Long id,
            @Parameter(description = "titre  du cours à mettre à jour")@RequestParam(value = "title", required = false) String title,
            @Parameter(description = "description du cours à mettre à jour")@RequestParam(value = "description", required = false) String description,
            @Parameter(description = "prix du cours à mettre à jour")@RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "file", required = false) MultipartFile file)  {
        try {
            LectureCourse updatedCourse = courseService.updateCourse(id,  title,  description, price, file);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Supprime un cours magistral", description = "Supprime un cours et son fichier associé")
    @ApiResponse(responseCode = "204", description = "Cours supprimé")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLectureCourse(
            @Parameter(description = "ID du cours à supprimer", required = true) @PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok("supression du support de cours effectué avec success");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCourses() {
        return ResponseEntity.ok(courseService.getTotalCourses());
    }
}
