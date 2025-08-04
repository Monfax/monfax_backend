package com.LDE.monFax_backend.controllers;


import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.services.CorrectionService;
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
@RequestMapping("/api/corrections")
@Tag(name = "Corrections", description = "API pour gérer les corrections d'epreuves")

@RequiredArgsConstructor
public class CorrectionController {

    private final CorrectionService correctionService;

    @GetMapping
    @Operation(
            summary = "Lister toutes les corrections",
            description = "Récupère la liste complète des corrections disponibles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste des corrections récupérée",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Correction.class))) } )
    public ResponseEntity<List<Correction>> getAllCorrections() {
        return ResponseEntity.ok(correctionService.getAllCorrections());
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer une correction par ID",
            description = "Retourne une correction correspondant à l'ID fourni",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correction trouvée",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Correction.class))),
                    @ApiResponse(responseCode = "404", description = "Correction non trouvée")
            }
    )
    public ResponseEntity<Correction> getCorrectionById(@PathVariable Long id) {
        return correctionService.getCorrectionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Créer une nouvelle correction",
            description = "Crée une correction liée à un examen donné avec un fichier uploadé",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correction créée avec succès"),
                    @ApiResponse(responseCode = "400", description = "Requête invalide (erreur lors de la création)")
            }
    )
    public ResponseEntity<String> createCorrection(
        @Parameter(description = "Titre de la correction", required = true)
        @RequestParam("title") String title,
        @Parameter(description = "Prix de la correction", required = true)
        @RequestParam("price") Double price,
        @Parameter(description = "ID de l'examen lié", required = true)
        @RequestParam("examId") Long examId,
        @Parameter(description = "Fichier de la correction au format PDF ou DOCX", required = true)
        @RequestParam("file") MultipartFile file) {
        try {
            Correction correction = correctionService.createCorrection(title, price, examId, file);
            return ResponseEntity.ok("creation de la correction effectuée avec success");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Mettre à jour une correction",
            description = "Mettre à jour une correction existante avec possibilité de changer le fichier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correction mise à jour avec succès",
                            content = @Content(schema = @Schema(implementation = Correction.class))),
                    @ApiResponse(responseCode = "404", description = "Correction non trouvée"),
                    @ApiResponse(responseCode = "500", description = "Erreur serveur")
            })
    @PutMapping("/{id}")
    public ResponseEntity<Correction> updateCorrection(
            @Parameter(description = "ID de la correction à mettre à jour", required = true)
            @PathVariable Long id,
            @RequestParam("title") String title,
            @Parameter(description = "Prix de la correction")
            @RequestParam("price") Double price,
            @Parameter(description = "Fichier corrigé à uploader", required = false)
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            Correction updated = correctionService.updateCorrection(id, title,price, file);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Operation(summary = "Supprime une correction",
            description = "Supprime une correction ainsi que son fichier associé.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Correction supprimée"),
                    @ApiResponse(responseCode = "404", description = "Correction non trouvée")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCorrection(
            @Parameter(description = "ID de la correction à supprimer", required = true)
            @PathVariable Long id) {
        try {
            String message = correctionService.deleteCorrection(id);
            if (message.startsWith("Erreur")) {
                // Si le message contient "Erreur", on renvoie un bad request ou not found
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur : " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getSubjectCount() {
        return ResponseEntity.ok(correctionService.getTotalCorrections());
    }
}
