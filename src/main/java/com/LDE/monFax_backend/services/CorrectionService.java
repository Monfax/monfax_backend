package com.LDE.monFax_backend.services;


import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.repositories.CorrectionRepository;
import com.LDE.monFax_backend.repositories.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public List<Correction> getAllCorrections() {
        return correctionRepository.findAll();
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
        // Upload fichier

        String fileUrl = resourceService.storeFile(file, "corrections");

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


    public Correction updateCorrection(Long id, String title,Double price, MultipartFile file) throws IOException {
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
            String originalFilename = (file.getOriginalFilename());
            String ext = resourceService.getExtension(originalFilename);
            if (!ext.equals("pdf") && !ext.equals("docx") ) {
                throw new IOException("format de fichier invalide ");
            }
            String newResourceUrl = resourceService.storeFile(file, "corrections");
            existingCorrection.setResourceUrl(newResourceUrl);
            existingCorrection.setSize(file.getSize());
        }

        return correctionRepository.save(existingCorrection);
    }
    public long getTotalCorrections() {
        return correctionRepository.count();
    }

}
