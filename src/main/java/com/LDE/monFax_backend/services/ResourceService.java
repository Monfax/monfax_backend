package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Resource;
import com.LDE.monFax_backend.repositories.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ResourceService {

    @Value("${resources.base-dir}")
    private String baseDir;
    private final ResourceRepository resourceRepository;


    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "docx","mp4");

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    public Resource saveResource(Resource resource) {
        resource.setCreatedAt(LocalDate.now());
        return resourceRepository.save(resource);
    }

    public String storeFile(MultipartFile file, String folderName) throws IOException {

        String originalName = file.getOriginalFilename();

        if (originalName == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide ou invalide.");
        }

        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extension non supportée : " + extension + ". Seuls les fichiers .pdf et .docx sont autorisés.");
        }

        String filename = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", folderName);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return "/uploads/" + folderName + "/" + filename;
    }

    public String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("Fichier sans extension : " + filename);
        }
        return filename.substring(lastDot + 1);
    }



    // Supprimer un fichier à partir de son chemin complet
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) return;
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }

    public byte[] downloadResource(Resource resource) throws IOException {
        if (resource.getResourceUrl() == null || resource.getResourceUrl().isEmpty()) {
            throw new IOException("Aucun fichier associé à cette ressource.");
        }



        Path path = Paths.get(baseDir+resource.getResourceUrl());
        return Files.readAllBytes(path);
    }

    public void increaseNumberOfViews(Resource resource){
        resource.setNumberOfView(resource.getNumberOfView() + 1);
    }
}
