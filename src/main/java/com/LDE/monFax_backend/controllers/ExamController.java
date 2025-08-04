package com.LDE.monFax_backend.controllers;


import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.models.LectureCourse;
import com.LDE.monFax_backend.services.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@Tag(name = "Exams", description = "API pour gérer les epreuves")

@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    @Operation(
            summary = "Lister tous les epreuves",
            description = "Récupère la liste complète des epreuves",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des epreuves récupérée",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exam.class)))
            }
    )
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer une epreuve par ID",
            description = "Retourne une epreuve  correspondant à l'ID fourni",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Examen trouvé",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exam.class))),
                    @ApiResponse(responseCode = "404", description = "Epreuve  non trouvé")
            })
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return examService.getExamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createExam(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam("year") int year,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        try {
            Exam exam = examService.createExam(title, type, year, subjectId, file);
            return ResponseEntity.ok("creation faite avec success");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExam(@PathVariable Long id) {

        try{
            examService.deleteExam(id);
            return ResponseEntity.ok("suppresion de l'epreuve effectuée avec success");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/subject/{subjectId}")
    @Operation(
            summary = "Lister les examens d'une matière",
            description = "Récupère tous les examens associés à un identifiant de matière",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des examens récupérée",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Exam.class))),
                    @ApiResponse(responseCode = "404", description = "Aucun examen trouvé pour cette matière")
            }
    )
    public ResponseEntity<List<Exam>> getExamsBySubjectId(@PathVariable Long subjectId) {
        List<Exam> exams = examService.getExamsBySubjectId(subjectId);
        if (exams.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(exams);
    }

    @Operation(summary = "Met à jour une epreuve", description = "Met à jour une epreuve avec possibilité de changer le fichier")
    @ApiResponse(responseCode = "200", description = "epreuve mise à jour", content = @Content(schema = @Schema(implementation = LectureCourse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(
            @Parameter(description = "ID du cours à mettre à jour", required = true) @PathVariable Long id,
            @Parameter(description = "titre  de l'epreuve  à mettre à jour")@RequestParam(value = "title", required = false) String title,
            @Parameter(description = "type de l'epreuve ( CONTINUOUS_ASSESSMENT, MAIN_EXAM,RESIT) à mettre à jour")@RequestParam(value = "examType", required = false) ExamType examType,
            @Parameter(description = "annee  de l'epreuve  à mettre à jour")@RequestParam(value = "year", required = false) int year,
            @RequestParam(value = "file", required = false) MultipartFile file)  {
        try {
            Exam updatedExam = examService.updateExam(id,  title,  examType, year, file);
            return ResponseEntity.ok(updatedExam);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/count")
    public ResponseEntity<Long> getExamsCount() {
        return ResponseEntity.ok(examService.getTotalExams());
    }

    @GetMapping("/count-by-type/{type}")
    public ResponseEntity<Long> getExamsCountByType(@PathVariable ExamType type) {
        return ResponseEntity.ok(examService.getExamsCountByType(type));
    }


}
