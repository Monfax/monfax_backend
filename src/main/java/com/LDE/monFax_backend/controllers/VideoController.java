package com.LDE.monFax_backend.controllers;


import com.LDE.monFax_backend.models.Video;
import com.LDE.monFax_backend.services.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "Gestion des vidéos pédagogiques (cours video)")
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    @Operation(summary = "Liste toutes les vidéos", description = "Retourne toutes les vidéos enregistrées")
    public ResponseEntity<List<Video>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une vidéo", description = "Retourne une vidéo spécifique par son identifiant")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        return videoService.getVideoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Créer un cours vidéo", description = "Crée un cours vidéo avec upload de fichier vidéo")
    public ResponseEntity<String> createVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("duration") Double duration,
            @RequestParam("price") Double price,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        try {
            Video video = videoService.createVideo(title, description, duration, price, subjectId, file);
            return ResponseEntity.ok("cours video cree avec success");
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un cours vidéo", description = "Supprime un cours vidéo par son identifiant")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateVideo(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            videoService.updateVideo(id, title, description, file);
            return ResponseEntity.ok("Vidéo mise à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }
    @GetMapping("/count")
    public ResponseEntity<Long> getSubjectCount() {
        return ResponseEntity.ok(videoService.getTotalVideo());
    }
}
