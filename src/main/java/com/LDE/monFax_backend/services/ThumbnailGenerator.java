package com.LDE.monFax_backend.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ThumbnailGenerator {
    /**
     * Génère une miniature à partir d'un fichier multipart.
     * @param file Le fichier à traiter.
     * @return Le chemin ou l'URL de la miniature générée.
     * @throws IOException si une erreur de traitement de fichier se produit.
     */
    String generateThumbnail(MultipartFile file) throws IOException;
}
