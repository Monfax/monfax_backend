package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Resource;
import com.LDE.monFax_backend.repositories.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

<<<<<<< HEAD
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
=======
// PDFBox imports
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

>>>>>>> 290ed71 (mis ajour ajout de thumbnail)

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
<<<<<<< HEAD
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extension non supportée : " + extension + ". Extensions autorisées : " + allowedExtensions);
=======
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extension non supportée : " + extension + ". Seuls les fichiers .pdf, .docx et .mp4 sont autorisés.");
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
        }

        String filename = UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", folderName);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return "/uploads/" + folderName + "/" + filename;
    }
    /**
     * Crée une miniature pour une ressource.
     * La logique de conversion dépend de l'extension du fichier.
     * @param file Le fichier téléversé.
     * @return Le chemin relatif de la miniature sauvegardée.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    public String createThumbnail(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("Nom de fichier original est null.");
        }
        String ext = getExtension(originalName);
        String thumbnailFilename = UUID.randomUUID().toString() + ".png"; // PNG est un bon format pour les miniatures

        // Créer le répertoire de miniatures s'il n'existe pas
        Path thumbnailUploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "thumbnails");
        Files.createDirectories(thumbnailUploadPath);

        Path thumbnailFilePath = thumbnailUploadPath.resolve(thumbnailFilename);

        BufferedImage thumbnailImage = null;

        // Logique de conversion selon l'extension
        switch (ext.toLowerCase()) {
            case "pdf":
                //  logique de conversion PDF :
                PDDocument document = PDDocument.load(file.getInputStream());
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                thumbnailImage = pdfRenderer.renderImageWithDPI(0, 100); // Rendre la première page à 100 DPI
                document.close();

                // Placeholder: si la bibliothèque n'est pas ajoutée, on peut créer une image simple
                // thumbnailImage = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);

                break;
            case "docx":
                // logique de conversion DOCX (complexe, ceci est un placeholder) :
                XWPFDocument doc = new XWPFDocument(file.getInputStream());
                

                // Placeholder: si la bibliothèque n'est pas ajoutée, on peut créer une image simple
                // thumbnailImage = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
                doc.close();
                break;
            case "mp4":
                
                thumbnailImage = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
                
                break;
            default:
                throw new IllegalArgumentException("Extension de fichier non prise en charge pour la miniature : " + ext);
        }

        if (thumbnailImage != null) {
            ImageIO.write(thumbnailImage, "png", thumbnailFilePath.toFile());
            return "/uploads/thumbnails/" + thumbnailFilename;
        }

        return null;
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
<<<<<<< HEAD
        Path path = Paths.get(baseDir + resource.getResourceUrl());
=======

        Path path = Paths.get(baseDir+resource.getResourceUrl());
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
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