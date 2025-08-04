package com.LDE.monFax_backend.repositories;


import com.LDE.monFax_backend.enumerations.ExamType;
import com.LDE.monFax_backend.models.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository  extends JpaRepository<Exam,Long> {
    List<Exam> findBySubjectId(Long subjectId);
    long count();
    long countByType(ExamType type);
}
