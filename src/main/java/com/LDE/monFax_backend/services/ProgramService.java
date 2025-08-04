package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.Program;
import com.LDE.monFax_backend.models.Semester;
import com.LDE.monFax_backend.repositories.DepartmentRepository;
import com.LDE.monFax_backend.repositories.ProgramRepository;
import com.LDE.monFax_backend.repositories.SemesterRepository;
import com.LDE.monFax_backend.requests.ProgramRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class ProgramService {
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final SemesterRepository semesterRepository;

    public Program createProgram(ProgramRequest programRequest) {
        Department department = departmentRepository.findById(programRequest.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Aucun departement avec l'id : " + programRequest.getDepartmentId()));
        Program program = new Program();
        program.setDepartment(department);
        program.setName(programRequest.getName());
        Program savedProgram = programRepository.save(program);

        Semester semester1 = new Semester();
        semester1.setProgram(program);
        semester1.setName("Semestre 1");
        semester1.setPrice(1000.0);
        semesterRepository.save(semester1);

        Semester semester2 = new Semester();
        semester2.setProgram(program);
        semester2.setName("Semestre 2");
        semester2.setPrice(1000.0);
        semesterRepository.save(semester2);

        return savedProgram;
    }

    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

    public Optional<Program> getProgramById(Long id) {
        return programRepository.findById(id);
    }

    public void updateProgram(Long id, ProgramRequest request) throws Exception {
        Optional<Program> optionalProgram = programRepository.findById(id);
        if (optionalProgram.isEmpty()) {
            throw new Exception("Departement non trouvée");
        }
        Program program = optionalProgram.get();

        if (request.getName() != null) program.setName(request.getName());
        if (request.getDepartmentId() != null){
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Aucun departement avec l'id : " + request.getDepartmentId()));
            program.setDepartment(department);
        }

        programRepository.save(program);
    }

    public boolean deleteProgram(Long id) {
        return programRepository.findById(id).map(program -> {
            programRepository.delete(program);
            return true;
        }).orElse(false);
    }
}
