package com.LDE.monFax_backend.TestService;

import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.repositories.CorrectionRepository;
import com.LDE.monFax_backend.repositories.ExamRepository;
import com.LDE.monFax_backend.services.CorrectionService;
import com.LDE.monFax_backend.services.ResourceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrectionServiceTest {

    @Mock
    private CorrectionRepository correctionRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private CorrectionService correctionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCorrections() {
        when(correctionRepository.findAll()).thenReturn(List.of(new Correction()));

        List<Correction> result = correctionService.getAllCorrections();

        assertEquals(1, result.size());
        verify(correctionRepository, times(1)).findAll();
    }

    @Test
    void testGetCorrectionById() {
        Correction correction = new Correction();
        correction.setId(1L);
        when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));

        Optional<Correction> result = correctionService.getCorrectionById(1L);

        assertTrue(result.isPresent());
        verify(resourceService, times(1)).increaseNumberOfViews(correction);
        verify(correctionRepository, times(1)).save(correction);
    }

    @Test
    void testCreateCorrection() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getSize()).thenReturn(1234L);
        when(mockFile.getOriginalFilename()).thenReturn("file.pdf");
        when(resourceService.storeFile(mockFile, "corrections", List.of("pdf", "docx")))
                .thenReturn("url/fichier.pdf");

        Exam exam = new Exam();
        exam.setId(10L);
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(correctionRepository.save(any(Correction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Correction result = correctionService.createCorrection("titre", 50.0, 10L, mockFile);

        assertNotNull(result);
        assertEquals("titre", result.getTitle());
        assertEquals(50.0, result.getPrice());
        assertEquals(1234L, result.getSize());
        assertEquals(exam, result.getExam());
        assertEquals("url/fichier.pdf", result.getResourceUrl());
        assertEquals(LocalDate.now(), result.getCreatedAt());

        verify(correctionRepository, times(1)).save(any(Correction.class));
    }

    @Test
    void testDeleteCorrection_Success() {
        Correction correction = new Correction();
        correction.setId(1L);
        Exam exam = new Exam();
        correction.setExam(exam);

        when(correctionRepository.existsById(1L)).thenReturn(true);
        when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));

        String result = correctionService.deleteCorrection(1L);

        assertEquals("suppression reussie!", result);
        verify(examRepository, times(1)).save(exam);
        verify(correctionRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCorrection_NotFound() {
        when(correctionRepository.existsById(99L)).thenReturn(false);

        String result = correctionService.deleteCorrection(99L);

        assertTrue(result.contains("n'existe pas"));
        verify(correctionRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateCorrection_WithNewFile() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("newFile.pdf");
        when(mockFile.getSize()).thenReturn(5000L);

        Correction existing = new Correction();
        existing.setId(1L);
        existing.setResourceUrl("old/url.pdf");

        when(correctionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(resourceService.getExtension("newFile.pdf")).thenReturn("pdf");
        when(resourceService.storeFile(mockFile, "corrections", List.of("pdf", "docx"))).thenReturn("new/url.pdf");
        when(correctionRepository.save(any(Correction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Correction updated = correctionService.updateCorrection(1L, "newTitle", 70.0, mockFile);

        assertEquals("newTitle", updated.getTitle());
        assertEquals(70.0, updated.getPrice());
        assertEquals("new/url.pdf", updated.getResourceUrl());
        assertEquals(5000L, updated.getSize());

        verify(resourceService, times(1)).deleteFile("old/url.pdf");
        verify(correctionRepository, times(1)).save(existing);
    }

    @Test
    void testGetTotalCorrections() {
        when(correctionRepository.count()).thenReturn(5L);

        long total = correctionService.getTotalCorrections();

        assertEquals(5L, total);
        verify(correctionRepository, times(1)).count();
    }
}
