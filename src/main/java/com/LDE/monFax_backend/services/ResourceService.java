package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.models.LectureCourse;
import com.LDE.monFax_backend.models.Resource;
import com.LDE.monFax_backend.models.Video;
import com.LDE.monFax_backend.repositories.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.AttributedString;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

@Service
@RequiredArgsConstructor
public class ResourceService {

    @Value("${resources.base-dir}")
    private String baseDir;

    private final ResourceRepository resourceRepository;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("pdf", "docx", "mp4");

    // Stocke un fichier et génère automatiquement sa vignette
    public Map<String, String> storeFileAndGenerateThumbnail(MultipartFile file, String folderName) throws IOException, JCodecException {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Le fichier est vide ou invalide.");
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Extension non supportée : " + extension);
        }

        // Crée le répertoire du fichier si nécessaire
        Path uploadPath = Paths.get(baseDir, folderName);
        Files.createDirectories(uploadPath);

        // Nom unique pour éviter les collisions
        String filename = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        // Génération automatique de la vignette
        String thumbnailPath = createThumbnail(file);

        // Retourne les chemins relatifs
        Map<String, String> result = new HashMap<>();
        result.put("filePath", "/uploads/" + folderName + "/" + filename);
        result.put("thumbnailPath", thumbnailPath);

        return result;
    }

   public String createThumbnail(MultipartFile file) throws IOException, JCodecException {
    String ext = getExtension(file.getOriginalFilename()).toLowerCase();
    String thumbnailFilename = UUID.randomUUID().toString() + ".png";

    Path thumbnailDir = Paths.get(baseDir, "thumbnails");
    Files.createDirectories(thumbnailDir);
    Path thumbnailPath = thumbnailDir.resolve(thumbnailFilename);

    BufferedImage thumbnailImage = null;

    switch (ext) {
        case "pdf" -> {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                thumbnailImage = pdfRenderer.renderImageWithDPI(0, 150);
            }
        }

        case "docx" -> {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {

                String firstPageText = extractor.getText();
                thumbnailImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = thumbnailImage.createGraphics();
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, 400, 400);
                g2d.setColor(java.awt.Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                drawText(g2d, firstPageText, 10, 20, 380);
                g2d.dispose();
            }
        }

        case "mp4" -> {
            // Stocke d'abord la vidéo
            Path videoDir = Paths.get(baseDir, "videos");
            Files.createDirectories(videoDir);
            String videoFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path videoPath = videoDir.resolve(videoFilename);
            file.transferTo(videoPath.toFile());

            // Crée la vignette
            File videoFile = videoPath.toFile();
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
            Picture picture = grab.getNativeFrame();
            if (picture != null) {
                thumbnailImage = AWTUtil.toBufferedImage(picture);
            } else {
                System.err.println("⚠️ Impossible de lire la première frame de la vidéo : " + file.getOriginalFilename());
            }
        }

        default -> throw new IllegalArgumentException("Extension non supportée pour la vignette : " + ext);
    }

    if (thumbnailImage != null) {
        ImageIO.write(thumbnailImage, "png", thumbnailPath.toFile());
        return "/uploads/thumbnails/" + thumbnailFilename;
    }

    return null;
}



    /** Méthode utilitaire pour écrire le texte avec retour à la ligne dans une image */
    private void drawText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
        for (String line : text.split("\n")) {
            AttributedString attrStr = new AttributedString(line);
            g2d.drawString(attrStr.getIterator(), x, y);
            y += g2d.getFontMetrics().getHeight();
        }
    }

    public String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) throw new IllegalArgumentException("Fichier sans extension : " + filename);
        return filename.substring(lastDot + 1);
    }

    // CRUD
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

    // Téléchargement d'un fichier
    public byte[] downloadResource(Resource resource) throws IOException {
        if (resource.getResourceUrl() == null || resource.getResourceUrl().isEmpty()) {
            throw new IOException("Aucun fichier associé à cette ressource.");
        }
        Path path = Paths.get(baseDir, resource.getResourceUrl().replaceFirst("/uploads/", ""));
        return Files.readAllBytes(path);
    }

    // Suppression physique d'un fichier
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) return;
        Path path = Paths.get(baseDir, filePath.replaceFirst("/uploads/", ""));
        Files.deleteIfExists(path);
    }

    // Incrémentation des vues
    public void increaseNumberOfViews(Exam exam) {
        exam.setNumberOfView(exam.getNumberOfView() + 1);
    }

    public void increaseNumberOfViews(LectureCourse course) {
        course.setNumberOfView(course.getNumberOfView() + 1);
    }

    public void increaseNumberOfViews(Video video) {
        video.setNumberOfView(video.getNumberOfView() + 1);
    }

    public void increaseNumberOfViews(Correction correction) {
        correction.setNumberOfView(correction.getNumberOfView() + 1);
    }

    /**
     * Stocke un fichier dans le dossier spécifié et retourne le chemin relatif.
     */
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

        Path uploadPath = Paths.get(baseDir, folderName);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return "/uploads/" + folderName + "/" + filename;
    }
}
