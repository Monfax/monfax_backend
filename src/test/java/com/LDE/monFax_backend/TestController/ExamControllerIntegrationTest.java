package com.LDE.monFax_backend.TestController;

import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.repositories.ExamRepository;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.LDE.monFax_backend.repositories.SubjectRepository;
import com.LDE.monFax_backend.repositories.UserRepository;
import com.LDE.monFax_backend.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExamControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ExamRepository examRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private User admin;
    private String jwtToken;
    private Subject subject;
    private Exam exam;

    @BeforeEach
    void setUp() {
        subjectRepository.deleteAll();
        examRepository.deleteAll();
        // Création d’un utilisateur admin
        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(com.LDE.monFax_backend.enumerations.UserType.ADMIN);
        userRepository.save(admin);
        jwtToken = jwtService.generateToken(admin);

        // Création d’un sujet
        subject = new Subject();
        subject.setName("Maths");
        subjectRepository.save(subject);
        System.out.println("Subject ID: " + subject.getId());

        // Création d’un examen initial
        exam = new Exam();
        exam.setTitle("Examen 1");
        exam.setType(ExamType.CONTINUOUS_ASSESSMENT);
        exam.setYear(2025);
        exam.setSubject(subject);
        exam.setNumberOfView(0L);
        exam.setCreatedAt(LocalDate.now());
        examRepository.save(exam);
    }

    @Test
void testCreateExam() throws Exception {
    // Fichier "virtuel" en mémoire
    MockMultipartFile file = new MockMultipartFile(
            "file",                       // doit correspondre à @RequestPart
            "RAPPORT_STAGE.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Test Content".getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart("/api/exams")
                    .file(file)
                    .param("title", "Examen 4")
                    .param("type", "CONTINUOUS_ASSESSMENT") // correspond à l'enum
                    .param("year", "2026")
                    .param("subjectId", String.valueOf(subject.getId()))
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print()) // Affiche le request/response complet pour debugging
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Examen 4"))
            .andExpect(jsonPath("$.type").value("CONTINUOUS_ASSESSMENT"))
            .andExpect(jsonPath("$.subject.id").value(subject.getId())); 
}


    @Test
    void testGetAllExams() throws Exception {
    mockMvc.perform(get("/api/exams")
                    .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.hasItem("Examen 1")));
}


    @Test
    void testGetExamById() throws Exception {
        mockMvc.perform(get("/api/exams/{id}", exam.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Examen 1"));
    }

    @Test
    void testDeleteExam() throws Exception {
        mockMvc.perform(delete("/api/exams/{id}", exam.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        boolean exists = examRepository.existsById(exam.getId());
        assertThat(exists).isFalse();
    }
}
