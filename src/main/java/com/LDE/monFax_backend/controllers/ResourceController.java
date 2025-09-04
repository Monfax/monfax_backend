package com.LDE.monFax_backend.controllers;


import com.LDE.monFax_backend.models.Resource;
import com.LDE.monFax_backend.repositories.ResourceRepository;
import com.LDE.monFax_backend.services.ResourceService;
import com.LDE.monFax_backend.services.ThumbnailGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    // Injecte les services et le repository nécessaires
    private final ResourceService resourceService;
    private final ResourceRepository resourceRepository;
    private final ThumbnailGenerator thumbnailService;

    // Injecte la valeur du répertoire de base à partir de application.properties
    @Value("${resources.base-dir}")
    private String baseDirectory;

    @GetMapping
    public ResponseEntity<List<Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        return resourceService.getResourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
    
    y
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Le fichier est vide.", HttpStatus.BAD_REQUEST);
        }

        try {
            // Sauvegarde du fichier original dans le répertoire spécifié
            Path originalFilePath = Paths.get(baseDirectory, file.getOriginalFilename());
            Files.write(originalFilePath, file.getBytes());

            // Génération de la vignette en utilisant le service dédié
            String thumbnailPath = thumbnailService.generateThumbnail(originalFilePath.toString());

            /

            return new ResponseEntity<>(
                "Fichier original et vignette générés avec succès. \n" +
                "Chemin du fichier original : " + originalFilePath.toString() + "\n" +
                "Chemin de la vignette : " + thumbnailPath, 
                HttpStatus.OK
            );

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Échec du téléchargement et de la génération de la vignette : " + e.getMessage(), 
                                        HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadResource(@PathVariable Long id) {
        try {
            Resource resource = resourceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ressource non trouvée en base"));

            System.out.println("Téléchargement ressource: " + resource.getTitle() + ", url=" + resource.getResourceUrl());

            byte[] data = resourceService.downloadResource(resource);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getTitle() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lecture fichier : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur inattendue : " + e.getMessage());
        }
    }
}
