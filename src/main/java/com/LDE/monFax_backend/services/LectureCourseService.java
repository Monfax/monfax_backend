package com.LDE.monFax_backend.services;


import com.LDE.monFax_backend.models.LectureCourse;
import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.models.Video;
import com.LDE.monFax_backend.repositories.LectureCourseRepository;
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
public class LectureCourseService {

    private final LectureCourseRepository lectureCourseRepository;
    private final SubjectRepository subjectRepository;
    private final ResourceService resourceService;

    public List<LectureCourse> getAllCourses() {
        return lectureCourseRepository.findAll();
    }

    public Optional<LectureCourse> getCourseById(Long id) {
        Optional<LectureCourse> course =  lectureCourseRepository.findById(id);
        course.ifPresent(foundCourse -> {
            resourceService.increaseNumberOfViews(foundCourse);
            lectureCourseRepository.save(foundCourse);
        });
        return course;
    }

    public LectureCourse createCourse(String title, String description, Double price, Long subjectId, MultipartFile file) throws IOException {
        String fileUrl = resourceService.storeFile(file, "courses");

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Matière introuvable avec l'id : " + subjectId));

        LectureCourse course = new LectureCourse();
        course.setTitle(title);
        course.setDescription(description);
        course.setPrice(price);
        course.setSubject(subject);
        course.setResourceUrl(fileUrl);
        course.setSize(file.getSize());
        course.setCreatedAt(LocalDate.now());
        course.setNumberOfDownload(0L);
        course.setNumberOfView(0L);

        return lectureCourseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        lectureCourseRepository.deleteById(id);
    }




    public LectureCourse updateCourse(Long id, String title, String description,Double price, MultipartFile file) throws IOException {
        LectureCourse lectureCourse = lectureCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("support de Cours  non trouvé"));

        if (title != null) lectureCourse.setTitle(title);
        if (description != null) lectureCourse.setDescription(description);
        if (price != null)lectureCourse.setPrice(price);
        if (file != null && !file.isEmpty()) {
            resourceService.deleteFile(lectureCourse.getResourceUrl());

            String originalFilename=(file.getOriginalFilename());
            String ext =resourceService.getExtension(originalFilename);
            if (!ext.equals("pdf") && !ext.equals("docx")) {

                throw new IOException("mauvais format de fichier ");

            }
            String fileName = resourceService.storeFile(file,"Courses");
            lectureCourse.setResourceUrl(fileName);
            lectureCourse.setSize(file.getSize());
        }

        return lectureCourseRepository.save(lectureCourse);
    }
    public long getTotalCourses() {
        return lectureCourseRepository.count();
    }

}
