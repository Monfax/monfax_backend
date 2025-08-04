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

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Optional<Exam> getExamById(Long id) {
        Optional<Exam> exam =  examRepository.findById(id);
        exam.ifPresent(foundExam -> {
            resourceService.increaseNumberOfViews(foundExam);
            examRepository.save(foundExam);
        });
        return exam;
    }

    public Exam createExam(String title, String type, int year, Long subjectId, MultipartFile file) throws IOException {
        // 1. Upload du fichier
        String filename = (file.getOriginalFilename());
        String ext = resourceService.getExtension(filename);
        if (!ext.equals("pdf") && !ext.equals("docx") ) {
            throw new IOException("format de fichier invalide ");
        }

        String fileUrl = resourceService.storeFile(file, "exams");

        // 2. Récupérer la matière
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet introuvable avec l'id : " + subjectId));

        // 3. Créer et sauvegarder l'examen
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setType(ExamType.valueOf(type.toUpperCase()));
        exam.setYear(year);
        exam.setSize(file.getSize());
        exam.setSubject(subject);
        exam.setResourceUrl(fileUrl);
        exam.setCreatedAt(LocalDate.now());
        exam.setNumberOfDownload(0L);
        exam.setNumberOfView(0L);

        return examRepository.save(exam);
    }

    public String deleteExam(Long id) {
        try {
            if (!examRepository.existsById(id)) {
                return "Erreur : L'epreuve avec l'id " + id + " n'existe pas.";
            }
            examRepository.deleteById(id);
            return "Suppression de l'epreuve avec l'id " + id + " réussie.";
        } catch (Exception e) {
            return "Erreur lors de la suppression de l'epreuve  : " + e.getMessage();
        }    }

    public Exam updateExam (Long id, String title, ExamType examType,int year, MultipartFile file) throws IOException {
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("epreuve  inexistante avec l'id " + id));

        // Mise à jour des champs simples
        if (title != null)  existingExam.setTitle(title);
        if (examType != null)  existingExam.setType(examType);
        if (year !=0)  existingExam.setYear(year);


        // Si un nouveau fichier est uploadé, on remplace l'ancien fichier
        if (file != null && !file.isEmpty()) {
            // Supprimer l'ancien fichier physique
            if (existingExam.getResourceUrl() != null) {
                resourceService.deleteFile(existingExam.getResourceUrl());
            }

            // Enregistrer le nouveau fichier et mettre à jour resourceUrl et size
            String originalFilename = (file.getOriginalFilename());
            String ext = resourceService.getExtension(originalFilename);
            if (!ext.equals("pdf") && !ext.equals("docx") ) {
                throw new IOException("format de fichier invalide ");
            }
            String newResourceUrl = resourceService.storeFile(file,"exams");
            existingExam.setResourceUrl(newResourceUrl);
            existingExam.setSize(file.getSize());
        }

        return examRepository.save(existingExam);
    }
    public long getTotalExams() {
        return examRepository.count();
    }
    public long getExamsCountByType(ExamType type) {
        return examRepository.countByType(type);
    }



    public List<Exam> getExamsBySubjectId(Long subjectId) {
        return examRepository.findBySubjectId(subjectId);
    }

}
