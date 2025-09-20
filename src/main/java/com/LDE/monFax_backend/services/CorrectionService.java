package com.LDE.monFax_backend.services;


import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.repositories.CorrectionRepository;
import com.LDE.monFax_backend.repositories.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;



import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final ExamRepository examRepository;
    private final ResourceService resourceService;
    private static final String DEFAULT_THUMBNAIL = "assets/default-pdf.png";

    public Page<Correction> getAllCorrections(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return correctionRepository.findAll(pageable);
    }

    public Optional<Correction> getCorrectionById(Long id) {
        Optional<Correction> correction =  correctionRepository.findById(id);
        correction.ifPresent(foundCorrection-> {
            resourceService.increaseNumberOfViews(foundCorrection);
            correctionRepository.save(foundCorrection);
        });
        return correction;
    }

    public Correction createCorrection(String title, Double price, Long examId, MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String ext = resourceService.getExtension(filename);
        if (!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("docx")) {
            throw new IOException("Format de fichier invalide (uniquement PDF ou DOCX).");
        }
        // Upload fichier

        String fileUrl = resourceService.storeFile(file, "corrections", List.of("pdf", "docx"));
        String thumbnailUrl;
        if (ext.equalsIgnoreCase("pdf")) {
            String absolutePath = System.getProperty("user.dir") + fileUrl;
            String thumbnailName = filename.replaceAll("\\.pdf$", "") + "_thumb";
            thumbnailUrl = resourceService.generatePdfThumbnailFromFile(absolutePath, System.getProperty("user.dir") + "/uploads/thumbnails", thumbnailName);
        } else {
            thumbnailUrl = DEFAULT_THUMBNAIL;
        }

        // Lier à l'examen
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("epreuve introuvable avec l'id : " + examId));


        // Création de l'objet Correction
        Correction correction = new Correction();
        correction.setTitle(title);
        correction.setPrice(price);
        correction.setSize(file.getSize());
        correction.setExam(exam);
        correction.setResourceUrl(fileUrl);
        correction.setCreatedAt(LocalDate.now());
        correction.setThumbnailUrl(thumbnailUrl);
        correction.setNumberOfDownload(0L);
        correction.setNumberOfView(0L);
        return correctionRepository.save(correction);
    }

    public String deleteCorrection(Long id) {
        try {
            boolean exists = correctionRepository.existsById(id);
            if (!exists) {
                return "Erreur : La correction avec l'id " + id + " n'existe pas.";
            }
            Correction correction = correctionRepository.findById(id).orElseThrow();

            // Rompre la relation avec l'exam
            Exam exam = correction.getExam();
            if (exam != null) {
                exam.setCorrection(null);  // enlève la référence à la correction
                examRepository.save(exam);  // sauvegarde la mise à jour
            }

            correctionRepository.deleteById(id);

        } catch (Exception e) {
            return "Erreur lors de la suppression de la correction : " + e.getMessage();
        }
        return "suppression reussie!";
    }


    public Correction updateCorrection(Long id, String title, Double price, MultipartFile file) throws IOException {
    Correction existingCorrection = correctionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Correction avec l'id  " + id + "introuvable"));

    if (title != null)  existingCorrection.setTitle(title);
    if (price != null) existingCorrection.setPrice(price);

    // Si un nouveau fichier est uploadé, on remplace l'ancien fichier
    if (file != null && !file.isEmpty()) {
        // Supprimer ancien fichier
        if (existingCorrection.getResourceUrl() != null) {
            resourceService.deleteFile(existingCorrection.getResourceUrl());
        }

        // Enregistrer nouveau fichier
        String originalFilename = file.getOriginalFilename();
        String ext = resourceService.getExtension(originalFilename);
        if (!ext.equals("pdf") && !ext.equals("docx")) {
            throw new IOException("format de fichier invalide ");
        }
        String newResourceUrl = resourceService.storeFile(file, "corrections", List.of("pdf", "docx"));
        existingCorrection.setResourceUrl(newResourceUrl);
        existingCorrection.setSize(file.getSize());
    }

    return correctionRepository.save(existingCorrection);
}
    public long getTotalCorrections() {
        return correctionRepository.count();
    }

}
