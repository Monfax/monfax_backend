package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.repositories.ExamRepository;
import com.LDE.monFax_backend.repositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final ResourceService resourceService;

    // ✅ Vignette par défaut
    private static final String DEFAULT_THUMBNAIL = "assets/default-pdf.png";

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Optional<Exam> getExamById(Long id) {
        Optional<Exam> exam = examRepository.findById(id);
        exam.ifPresent(foundExam -> {
            resourceService.increaseNumberOfViews(foundExam);
            examRepository.save(foundExam);
        });
        return exam;
    }

    public Exam createExam(String title, String type, int year, Long subjectId,
                           MultipartFile file, MultipartFile thumbnail) throws IOException {
        // 1. Vérifier et sauvegarder le fichier exam
        String filename = file.getOriginalFilename();
        String ext = resourceService.getExtension(filename);
        if (!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("docx")) {
            throw new IOException("Format de fichier invalide (uniquement PDF ou DOCX).");
        }
        String fileUrl = resourceService.storeFile(file, "exams");

        // 2. Sauvegarder la vignette
        String thumbnailUrl;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbExt = resourceService.getExtension(thumbnail.getOriginalFilename());
            if (!thumbExt.equalsIgnoreCase("png") &&
                !thumbExt.equalsIgnoreCase("jpg") &&
                !thumbExt.equalsIgnoreCase("jpeg")) {
                throw new IOException("Format de vignette invalide (uniquement PNG, JPG ou JPEG).");
            }
            thumbnailUrl = resourceService.storeFile(thumbnail, "thumbnails");
        } else {
            thumbnailUrl = DEFAULT_THUMBNAIL;
        }

        // 3. Récupérer la matière
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet introuvable avec l'id : " + subjectId));

        // 4. Créer et sauvegarder l'examen
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setType(ExamType.valueOf(type.toUpperCase()));
        exam.setYear(year);
        exam.setSize(file.getSize());
        exam.setSubject(subject);
        exam.setResourceUrl(fileUrl);
        exam.setThumbnailUrl(thumbnailUrl); // ✅ sauvegarde
        exam.setCreatedAt(LocalDate.now());
        exam.setNumberOfDownload(0L);
        exam.setNumberOfView(0L);

        return examRepository.save(exam);
    }

    public String deleteExam(Long id) {
        try {
            if (!examRepository.existsById(id)) {
                return "Erreur : L'épreuve avec l'id " + id + " n'existe pas.";
            }
            examRepository.deleteById(id);
            return "Suppression de l'épreuve avec l'id " + id + " réussie.";
        } catch (Exception e) {
            return "Erreur lors de la suppression de l'épreuve : " + e.getMessage();
        }
    }

    public Exam updateExam(Long id, String title, ExamType examType, int year,
                           MultipartFile file, MultipartFile thumbnail) throws IOException {
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épreuve inexistante avec l'id " + id));

        // Mise à jour des champs simples
        if (title != null) existingExam.setTitle(title);
        if (examType != null) existingExam.setType(examType);
        if (year != 0) existingExam.setYear(year);

        // Mise à jour du fichier exam
        if (file != null && !file.isEmpty()) {
            if (existingExam.getResourceUrl() != null) {
                resourceService.deleteFile(existingExam.getResourceUrl());
            }
            String ext = resourceService.getExtension(file.getOriginalFilename());
            if (!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("docx")) {
                throw new IOException("Format de fichier invalide (uniquement PDF ou DOCX).");
            }
            String newResourceUrl = resourceService.storeFile(file, "exams");
            existingExam.setResourceUrl(newResourceUrl);
            existingExam.setSize(file.getSize());
        }

        // Mise à jour de la vignette
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String thumbExt = resourceService.getExtension(thumbnail.getOriginalFilename());
            if (!thumbExt.equalsIgnoreCase("png") &&
                !thumbExt.equalsIgnoreCase("jpg") &&
                !thumbExt.equalsIgnoreCase("jpeg")) {
                throw new IOException("Format de vignette invalide (uniquement PNG, JPG ou JPEG).");
            }

            if (existingExam.getThumbnailUrl() != null &&
                    !existingExam.getThumbnailUrl().equals(DEFAULT_THUMBNAIL)) {
                resourceService.deleteFile(existingExam.getThumbnailUrl());
            }

            String newThumbnailUrl = resourceService.storeFile(thumbnail, "thumbnails");
            existingExam.setThumbnailUrl(newThumbnailUrl);
        }

        return examRepository.save(existingExam);
    }
}
