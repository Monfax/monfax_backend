package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Department;
import com.LDE.monFax_backend.models.Faculty;
import com.LDE.monFax_backend.repositories.FacultyRepository;
import com.LDE.monFax_backend.requests.FacultyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class FacultyService {

        private final FacultyRepository facultyRepository;

        public Faculty createFaculty(FacultyRequest request) {
            Faculty faculty = new Faculty();
            faculty.setName(request.getName());
            return facultyRepository.save(faculty);
        }

        public List<Faculty> getAllFaculties() {
            return facultyRepository.findAll();
        }

        public Optional<Faculty> getFacultyById(Long id) {
            return facultyRepository.findById(id);
        }

        public Faculty updateFaculty(Long id, FacultyRequest request) throws Exception {
            Optional<Faculty> optionalFaculty = facultyRepository.findById(id);
            if (optionalFaculty.isEmpty()) {
                throw new Exception("Faculte non trouvée");
            }
            Faculty faculty = optionalFaculty.get();

            if (request.getName() != null) faculty.setName(request.getName());
            return  facultyRepository.save(faculty);
        }

        public boolean deleteFaculty(Long id) {
            return facultyRepository.findById(id).map(faculty -> {
                facultyRepository.delete(faculty);
                return true;
            }).orElse(false);
        }
    }

