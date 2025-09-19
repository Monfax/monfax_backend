package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.services.ExamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/exams")
@Tag(name = "Exams", description = "API pour gérer les épreuves")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return examService.getExamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createExam(
        @RequestParam("title") String title,
        @RequestParam("type") String type,
        @RequestParam("year") int year,
        @RequestParam("subjectId") Long subjectId,
        @RequestPart("file") MultipartFile file) {
    try {
        Exam exam = examService.createExam(title, type, year, subjectId, file);
        return ResponseEntity.ok(exam);
    } catch (IllegalArgumentException | IOException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateExam(
        @PathVariable Long id,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "examType", required = false) ExamType examType,
        @RequestParam(value = "year", required = false, defaultValue = "0") int year,
        @RequestPart(value = "file", required = false) MultipartFile file) {
    try {
        Exam updatedExam = examService.updateExam(id, title, examType, year, file);
        return ResponseEntity.ok(updatedExam);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
    }
}

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExam(@PathVariable Long id) {
        try {
            String result = examService.deleteExam(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}