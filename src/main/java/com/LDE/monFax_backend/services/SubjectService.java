package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.models.Subject;
import com.LDE.monFax_backend.models.Semester;
import com.LDE.monFax_backend.repositories.SemesterRepository;
import com.LDE.monFax_backend.repositories.SubjectRepository;
import com.LDE.monFax_backend.requests.SubjectRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;

    public Subject createSubject(SubjectRequest subjectRequest) {
        Semester Semester = semesterRepository.findById(subjectRequest.getSemesterId())
                .orElseThrow(() -> new IllegalArgumentException("Aucun Semestre avec l'id : " + subjectRequest.getSemesterId()));
        Subject subject = new Subject();
        subject.setSemester(Semester);
        subject.setName(subjectRequest.getName());
        subject.setPrice(subjectRequest.getPrice());
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    public void updateSubject(Long id, SubjectRequest request) throws Exception {
        Optional<Subject> optionalSubject = subjectRepository.findById(id);
        if (optionalSubject.isEmpty()) {
            throw new Exception("Matiere non trouvée");
        }
        Subject subject = optionalSubject.get();

        if (request.getName() != null) subject.setName(request.getName());
        if (request.getPrice() != null) subject.setPrice(request.getPrice());
        if (request.getSemesterId() != null){
            Semester Semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new IllegalArgumentException("Aucun Matiere avec l'id : " + request.getSemesterId()));
            subject.setSemester(Semester);
        }

        subjectRepository.save(subject);
    }

    public boolean deleteSubject(Long id) {
        return subjectRepository.findById(id).map(subject -> {
            subjectRepository.delete(subject);
            return true;
        }).orElse(false);
    }
}
