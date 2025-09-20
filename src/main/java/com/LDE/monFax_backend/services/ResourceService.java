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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Service
@RequiredArgsConstructor
public class ResourceService {

    @Value("${resources.base-dir}")
    private String baseDir;
    private final ResourceRepository resourceRepository;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "docx", "mp4");

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

    public String storeFile(MultipartFile file, String folderName, List<String> allowedExtensions) throws IOException {
        String originalName = file.getOriginalFilename();

        if (originalName == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide ou invalide.");
        }

        String extension = getExtension(originalName);
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extension non supportée : " + extension + ". Extensions autorisées : " + allowedExtensions);
        }

        String filename = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", folderName);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return "/uploads/" + folderName + "/" + filename;
    }

    // Ajoute cette méthode pour l'extraction d'extension
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
        Path path = Paths.get(baseDir + resource.getResourceUrl());
        return Files.readAllBytes(path);
    }
    public String generatePdfThumbnailFromFile(String pdfPath, String thumbnailsDir, String thumbnailName) throws IOException {
    File dir = new File(thumbnailsDir);
    if (!dir.exists()) dir.mkdirs();

    try (PDDocument document = PDDocument.load(new File(pdfPath))) {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
        String thumbnailPath = thumbnailsDir + "/" + thumbnailName + ".png";
        ImageIO.write(bim, "png", new File(thumbnailPath));
        return "/uploads/thumbnails/" + thumbnailName + ".png";
    }
}
public String generateVideoThumbnail(String videoPath, String outputDir, String thumbnailName) throws IOException, InterruptedException {
    File dir = new File(outputDir);
    if (!dir.exists()) dir.mkdirs();

    String thumbnailPath = outputDir + File.separator + thumbnailName + ".png";

    // Commande FFmpeg pour extraire une image à 1 seconde
    String[] cmd = {"ffmpeg", "-i", videoPath, "-ss", "00:00:01.000", "-vframes", "1", thumbnailPath};
    Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
    int exitCode = process.waitFor();

    if (exitCode != 0) {
        throw new IOException("Erreur lors de la génération de la vignette pour la vidéo : " + videoPath);
    }

    return thumbnailPath.replace(System.getProperty("user.dir"), ""); // retourne le chemin relatif
}


    public void increaseNumberOfViews(Resource resource) {
        resource.setNumberOfView(resource.getNumberOfView() + 1);
    }
    // Génération automatique d'un thumbnail à partir d'un PDF
    public String generatePdfThumbnail(MultipartFile pdfFile, String thumbnailsDir, String thumbnailName) throws IOException {
        File dir = new File(thumbnailsDir);
        if (!dir.exists()) dir.mkdirs();

        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150); // première page, 150 DPI
            String thumbnailPath = thumbnailsDir + "/" + thumbnailName + ".png";
            ImageIO.write(bim, "png", new File(thumbnailPath));
            // Retourne le chemin relatif pour stockage en base
            return "/uploads/thumbnails/" + thumbnailName + ".png";
        }
    }
}