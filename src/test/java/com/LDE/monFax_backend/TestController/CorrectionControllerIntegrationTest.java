package com.LDE.monFax_backend.TestController;

import com.LDE.monFax_backend.controllers.CorrectionController;
import com.LDE.monFax_backend.models.Correction;
import com.LDE.monFax_backend.services.CorrectionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorrectionController.class)
class CorrectionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorrectionService correctionService;

    @Test
    void testGetAllCorrections() throws Exception {
        Correction c1 = new Correction();
        c1.setId(1L);
        c1.setTitle("Correction 1");
        c1.setPrice(1000.0);

        Correction c2 = new Correction();
        c2.setId(2L);
        c2.setTitle("Correction 2");
        c2.setPrice(2000.0);

        Mockito.when(correctionService.getAllCorrections()).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(get("/api/corrections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Correction 1")))
                .andExpect(jsonPath("$[1].title", is("Correction 2")));
    }

    @Test
    void testGetCorrectionById_Found() throws Exception {
        Correction c1 = new Correction();
        c1.setId(1L);
        c1.setTitle("Correction 1");
        c1.setPrice(1000.0);

        Mockito.when(correctionService.getCorrectionById(1L)).thenReturn(Optional.of(c1));

        mockMvc.perform(get("/api/corrections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Correction 1")))
                .andExpect(jsonPath("$.price", is(1000.0)));
    }

    @Test
    void testGetCorrectionById_NotFound() throws Exception {
        Mockito.when(correctionService.getCorrectionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/corrections/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCorrection() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes());

        Correction correction = new Correction();
        correction.setId(1L);
        correction.setTitle("New Correction");
        correction.setPrice(1500.0);

        Mockito.when(correctionService.createCorrection(eq("New Correction"), eq(1500.0), eq(10L), any()))
                .thenReturn(correction);

        mockMvc.perform(multipart("/api/corrections")
                        .file(file)
                        .param("title", "New Correction")
                        .param("price", "1500")
                        .param("examId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("creation de la correction effectuée avec success"));
    }

    @Test
    void testUpdateCorrection() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "update.pdf",
                MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes());

        Correction updated = new Correction();
        updated.setId(1L);
        updated.setTitle("Updated Correction");
        updated.setPrice(2000.0);

        Mockito.when(correctionService.updateCorrection(eq(1L), eq("Updated Correction"), eq(2000.0), any()))
                .thenReturn(updated);

        mockMvc.perform(multipart("/api/corrections/1")
                        .file(file)
                        .with(req -> { req.setMethod("PUT"); return req; }) // Trick pour multipart PUT
                        .param("title", "Updated Correction")
                        .param("price", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Correction")))
                .andExpect(jsonPath("$.price", is(2000.0)));
    }

    @Test
    void testDeleteCorrection() throws Exception {
        Mockito.when(correctionService.deleteCorrection(1L)).thenReturn("Correction supprimée");

        mockMvc.perform(delete("/api/corrections/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Correction supprimée"));
    }

    @Test
    void testGetSubjectCount() throws Exception {
        Mockito.when(correctionService.getTotalCorrections()).thenReturn(5L);

        mockMvc.perform(get("/api/corrections/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
