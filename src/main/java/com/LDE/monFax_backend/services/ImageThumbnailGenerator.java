package com.LDE.monFax_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Un service pour générer des miniatures pour les fichiers images.
 * Cette implémentation utilise les bibliothèques Java standard.
 */
@Service
public class ImageThumbnailGenerator implements ThumbnailGenerator {

    // Définir le répertoire de téléchargement pour les miniatures
    private final String uploadDir = "uploads/thumbnails";

    // Définir la largeur et la hauteur cibles pour la miniature
    private static final int THUMBNAIL_WIDTH = 150;
    private static final int THUMBNAIL_HEIGHT = 150;

    /**
     * Génère une miniature pour un fichier image.
     * La méthode lit l'image, la redimensionne et enregistre la nouvelle miniature.
     *
     * @param file Le fichier image à partir duquel générer une miniature.
     * @return L'URL/le chemin de la miniature générée.
     * @throws IOException si une erreur se produit lors du traitement de l'image.
     */
    @Override
    public String generateThumbnail(MultipartFile file) throws IOException {
        System.out.println("Génération d'une miniature pour l'image : " + file.getOriginalFilename());

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Lire l'image originale à partir du flux d'entrée du fichier multipart
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Le fichier n'est pas une image valide.");
        }

        // Créer une nouvelle image tampon pour la miniature avec la taille souhaitée
        BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Redimensionner l'image originale pour l'adapter à la miniature
        thumbnail.getGraphics().drawImage(originalImage, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);

        // Générer un nom de fichier unique pour la miniature
        String filename = UUID.randomUUID().toString() + ".jpg";
        Path thumbnailPath = uploadPath.resolve(filename);

        // Enregistrer la miniature sur le système de fichiers
        File outputFile = thumbnailPath.toFile();
        ImageIO.write(thumbnail, "jpg", outputFile);

        // Retourner le chemin relatif de la miniature enregistrée
        return "/" + uploadDir + "/" + filename;
    }
}
