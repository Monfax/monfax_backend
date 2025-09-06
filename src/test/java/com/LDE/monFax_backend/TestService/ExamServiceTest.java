package com.LDE.monFax_backend.TestService;

import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.repositories.ExamRepository;
import com.LDE.monFax_backend.repositories.SubjectRepository;
import com.LDE.monFax_backend.services.ExamService;
import com.LDE.monFax_backend.services.ResourceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ResourceService resourceService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ExamService examService;

    private Subject subject;
    private Exam exam;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Math");

        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Algebra");
        exam.setType(ExamType.CONTINUOUS_ASSESSMENT);
        exam.setYear(2025);
        exam.setSubject(subject);
        exam.setCreatedAt(LocalDate.now());
    }

    @Test
    void testGetAllExams() {
        when(examRepository.findAll()).thenReturn(List.of(exam));

        List<Exam> result = examService.getAllExams();

        assertEquals(1, result.size());
        assertEquals("Algebra", result.get(0).getTitle());
        verify(examRepository, times(1)).findAll();
    }

    @Test
    void testGetExamById_Found() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));

        Optional<Exam> result = examService.getExamById(1L);

        assertTrue(result.isPresent());
        verify(resourceService, times(1)).increaseNumberOfViews(exam);
        verify(examRepository, times(1)).save(exam);
    }

    @Test
    void testGetExamById_NotFound() {
        when(examRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Exam> result = examService.getExamById(99L);

        assertTrue(result.isEmpty());
        verify(resourceService, never()).increaseNumberOfViews(any());
    }

    @Test
    void testCreateExam_WithPdfFile() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("exam.pdf");
        when(multipartFile.getSize()).thenReturn(500L);
        when(resourceService.getExtension("exam.pdf")).thenReturn("pdf");
        when(resourceService.storeFile(eq(multipartFile), eq("exams"), anyList())).thenReturn("/uploads/exam.pdf");
        when(resourceService.generatePdfThumbnailFromFile(any(), any(), any())).thenReturn("/uploads/thumbnails/exam_thumb.png");
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exam result = examService.createExam("Physics", "CONTINUOUS_ASSESSMENT", 2025, 1L, multipartFile);

        assertNotNull(result);
        assertEquals("Physics", result.getTitle());
        assertEquals("/uploads/thumbnails/exam_thumb.png", result.getThumbnailUrl());
    }

    @Test
    void testCreateExam_InvalidFormat() {
        when(multipartFile.getOriginalFilename()).thenReturn("exam.txt");
        when(resourceService.getExtension("exam.txt")).thenReturn("txt");

        assertThrows(IOException.class,
                () -> examService.createExam("Physics", "CONTINUOUS_ASSESSMENT", 2025, 1L, multipartFile));
    }

    @Test
    void testDeleteExam_Success() {
        when(examRepository.existsById(1L)).thenReturn(true);

        String result = examService.deleteExam(1L);

        assertTrue(result.contains("réussie"));
        verify(examRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteExam_NotFound() {
        when(examRepository.existsById(99L)).thenReturn(false);

        String result = examService.deleteExam(99L);

        assertTrue(result.contains("n'existe pas"));
        verify(examRepository, never()).deleteById(anyLong());
    }

@Test
void testUpdateExam_UpdateTitleAndFilePdf() throws IOException {
    exam.setResourceUrl("/uploads/old.pdf"); //  permet d'appeler deleteFile

    when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("updated.pdf");
    when(multipartFile.getSize()).thenReturn(800L);
    when(resourceService.getExtension("updated.pdf")).thenReturn("pdf");
    when(resourceService.storeFile(any(), eq("exams"), anyList())).thenReturn("/uploads/updated.pdf");
    when(resourceService.generatePdfThumbnailFromFile(any(), any(), any())).thenReturn("/uploads/thumbnails/updated_thumb.png");
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Exam result = examService.updateExam(1L, "Updated Exam", ExamType.CONTINUOUS_ASSESSMENT, 2026, multipartFile);

    assertEquals("Updated Exam", result.getTitle());
    assertEquals(ExamType.CONTINUOUS_ASSESSMENT, result.getType());
    assertEquals(2026, result.getYear());
    assertEquals("/uploads/thumbnails/updated_thumb.png", result.getThumbnailUrl());

    // Vérifie que deleteFile est bien appelé avec l'ancienne ressource
    verify(resourceService, times(1)).deleteFile("/uploads/old.pdf");
}

    @Test
    void testUpdateExam_InvalidFormat() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("bad.txt");
        when(resourceService.getExtension("bad.txt")).thenReturn("txt");

        assertThrows(IOException.class,
                () -> examService.updateExam(1L, "Bad", ExamType.CONTINUOUS_ASSESSMENT, 2025, multipartFile));
    }

    @Test
    void testGetTotalExams() {
        when(examRepository.count()).thenReturn(5L);

        long result = examService.getTotalExams();

        assertEquals(5L, result);
    }
}
