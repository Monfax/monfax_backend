package com.LDE.monFax_backend.TestService;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.Faculty;
import com.LDE.monFax_backend.repositories.DepartmentRepository;
import com.LDE.monFax_backend.repositories.FacultyRepository;
import com.LDE.monFax_backend.requests.DepartmentRequest;
import com.LDE.monFax_backend.services.DepartmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Faculty faculty;
    private Department department;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Informatique");

        department = new Department();
        department.setId(10L);
        department.setName("Développement logiciel");
        department.setFaculty(faculty);
    }

    @Test
    void testCreateDepartment_Success() {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Mathématiques");
        request.setFacultyId(1L);

        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department created = departmentService.createDepartment(request);

        assertNotNull(created);
        assertEquals("Développement logiciel", created.getName()); // car mock renvoie `department`
        verify(facultyRepository, times(1)).findById(1L);
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testCreateDepartment_FacultyNotFound() {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Mathématiques");
        request.setFacultyId(2L);

        when(facultyRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> departmentService.createDepartment(request));
        verify(facultyRepository, times(1)).findById(2L);
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<Department> result = departmentService.getAllDepartments();

        assertEquals(1, result.size());
        assertEquals("Développement logiciel", result.get(0).getName());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetDepartmentById_Found() {
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        Optional<Department> result = departmentService.getDepartmentById(10L);

        assertTrue(result.isPresent());
        assertEquals("Développement logiciel", result.get().getName());
        verify(departmentRepository, times(1)).findById(10L);
    }

    @Test
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Department> result = departmentService.getDepartmentById(99L);

        assertFalse(result.isPresent());
        verify(departmentRepository, times(1)).findById(99L);
    }

    @Test
    void testUpdateDepartment_Success() throws Exception {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Nouveau département");
        request.setFacultyId(1L);

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        departmentService.updateDepartment(10L, request);

        assertEquals("Nouveau département", department.getName());
        assertEquals(faculty, department.getFaculty());
        verify(departmentRepository, times(1)).save(department);
    }

    @Test
    void testUpdateDepartment_NotFound() {
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Test");

        when(departmentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> departmentService.updateDepartment(100L, request));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testDeleteDepartment_Success() {
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        boolean result = departmentService.deleteDepartment(10L);

        assertTrue(result);
        verify(departmentRepository, times(1)).delete(department);
    }

    @Test
    void testDeleteDepartment_NotFound() {
        when(departmentRepository.findById(50L)).thenReturn(Optional.empty());

        boolean result = departmentService.deleteDepartment(50L);

        assertFalse(result);
        verify(departmentRepository, never()).delete(any());
    }
}
