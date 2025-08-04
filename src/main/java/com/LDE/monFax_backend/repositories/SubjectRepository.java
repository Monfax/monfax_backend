package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject,Long> {
    long count();
}
