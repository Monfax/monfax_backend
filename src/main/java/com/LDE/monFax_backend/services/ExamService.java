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

    public Exam createExam(String title, String type, int year, Long subjectId, MultipartFile file) throws IOException {
<<<<<<< HEAD
        String filename = file.getOriginalFilename();
=======
        // 1. Gérer le fichier téléversé et vérifier le format
        String filename = (file.getOriginalFilename());
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
        String ext = resourceService.getExtension(filename);
        if (!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("docx")) {
            throw new IOException("Format de fichier invalide (uniquement PDF ou DOCX).");
        }
        // 1. Stocker le fichier sur disque
        String fileUrl = resourceService.storeFile(file, "exams", List.of("pdf", "docx"));

        // 2. Générer automatiquement la vignette à partir du fichier stocké si PDF
        String thumbnailUrl;
        if (ext.equalsIgnoreCase("pdf")) {
            String absolutePath = System.getProperty("user.dir") + fileUrl;
            String thumbnailName = filename.replaceAll("\\.pdf$", "") + "_thumb";
            thumbnailUrl = resourceService.generatePdfThumbnailFromFile(absolutePath, System.getProperty("user.dir") + "/uploads/thumbnails", thumbnailName);
        } else {
            thumbnailUrl = DEFAULT_THUMBNAIL;
        }

<<<<<<< HEAD
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet introuvable avec l'id : " + subjectId));

=======
        // Stockage du fichier principal
        String fileUrl = resourceService.storeFile(file, "exams");

        // 2. Créer le fichier de la miniature
        String thumbnailUrl = resourceService.createThumbnail(file);

        // 3. Récupérer la matière
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet introuvable avec l'id : " + subjectId));

        // 4. Créer et sauvegarder l'examen, en associant la miniature
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setType(ExamType.valueOf(type.toUpperCase()));
        exam.setYear(year);
        exam.setSize(file.getSize());
        exam.setSubject(subject);
        exam.setResourceUrl(fileUrl);
<<<<<<< HEAD
        exam.setThumbnailUrl(thumbnailUrl);
=======
        exam.setThumbnailUrl(thumbnailUrl); // On associe la nouvelle URL
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
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

    public Exam updateExam(Long id, String title, ExamType examType, int year, MultipartFile file) throws IOException {
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Épreuve inexistante avec l'id " + id));

        if (title != null) existingExam.setTitle(title);
        if (examType != null) existingExam.setType(examType);
        if (year != 0) existingExam.setYear(year);

        if (file != null && !file.isEmpty()) {
            if (existingExam.getResourceUrl() != null) {
                resourceService.deleteFile(existingExam.getResourceUrl());
            }
            String ext = resourceService.getExtension(file.getOriginalFilename());
            if (!ext.equalsIgnoreCase("pdf") && !ext.equalsIgnoreCase("docx")) {
                throw new IOException("Format de fichier invalide (uniquement PDF ou DOCX).");
            }
            String newResourceUrl = resourceService.storeFile(file, "exams", List.of("pdf", "docx"));
            existingExam.setResourceUrl(newResourceUrl);
            existingExam.setSize(file.getSize());

<<<<<<< HEAD
            if (ext.equalsIgnoreCase("pdf")) {
                String absolutePath = System.getProperty("user.dir") + newResourceUrl;
                String thumbnailName = file.getOriginalFilename().replaceAll("\\.pdf$", "") + "_thumb";
                String newThumbnailUrl = resourceService.generatePdfThumbnailFromFile(absolutePath, System.getProperty("user.dir") + "/uploads/thumbnails", thumbnailName);
                existingExam.setThumbnailUrl(newThumbnailUrl);
            } else {
                existingExam.setThumbnailUrl(DEFAULT_THUMBNAIL);
            }
=======
            // Créer et associer une nouvelle miniature
            String newThumbnailUrl = resourceService.createThumbnail(file);
            existingExam.setThumbnailUrl(newThumbnailUrl);
>>>>>>> 290ed71 (mis ajour ajout de thumbnail)
        }

        return examRepository.save(existingExam);
    }
    public long getTotalExams(){
        return examRepository.count();
    }
}
