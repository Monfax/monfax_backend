package com.LDE.monFax_backend.TestController;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.repositories.DepartmentRepository;
import com.LDE.monFax_backend.repositories.UserRepository;
import com.LDE.monFax_backend.requests.DepartmentRequest;
import com.LDE.monFax_backend.enumerations.UserType;
import com.LDE.monFax_backend.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Department department;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Création d’un département initial
        department = new Department();
        department.setName("Informatique");
        departmentRepository.save(department);

        // Création d’un utilisateur admin pour générer le token
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(UserType.ADMIN);
        userRepository.save(admin);

        // Génération du token JWT
        jwtToken = jwtService.generateToken(admin);
    }

    @Test
    void testCreateDepartment() throws Exception {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Mathematiques");

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Mathematiques")));
    }

    @Test
    void testGetAllDepartments() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Informatique")));
    }

    @Test
    void testGetDepartmentById() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", department.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Informatique")));
    }

    @Test
    void testGetDepartmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", 9999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDepartment() throws Exception {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Physique");

        mockMvc.perform(put("/api/departments/{id}", department.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Departement mise à jour avec succès"));

        // Vérification en base
        Department updated = departmentRepository.findById(department.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Physique");
    }

    @Test
    void testDeleteDepartment() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", department.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("success to delete department"));

        // Vérification en base
        boolean exists = departmentRepository.existsById(department.getId());
        assertThat(exists).isFalse();
    }
}
