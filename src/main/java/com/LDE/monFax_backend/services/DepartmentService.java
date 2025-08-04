package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.Faculty;
import com.LDE.monFax_backend.repositories.DepartmentRepository;
import com.LDE.monFax_backend.repositories.FacultyRepository;
import com.LDE.monFax_backend.requests.DepartmentRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    public Department createDepartment(DepartmentRequest departmentRequest) {
        Faculty faculty = facultyRepository.findById(departmentRequest.getFacultyId())
                .orElseThrow(() -> new IllegalArgumentException("Aucun departement avec l'id : " + departmentRequest.getFacultyId()));
        Department department = new Department();
        department.setFaculty(faculty);
        department.setName(departmentRequest.getName());
        return departmentRepository.save(department);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public void updateDepartment(Long id, DepartmentRequest request) throws Exception {
        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        if (optionalDepartment.isEmpty()) {
            throw new Exception("Departement non trouvée");
        }
        Department department = optionalDepartment.get();

        if (request.getName() != null) department.setName(request.getName());
        if (request.getFacultyId() != null){
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new IllegalArgumentException("Aucun departement avec l'id : " + request.getFacultyId()));
            department.setFaculty(faculty);
        }

        departmentRepository.save(department);
    }

    public boolean deleteDepartment(Long id) {
        return departmentRepository.findById(id).map(department -> {
            departmentRepository.delete(department);
            return true;
        }).orElse(false);
    }
}
