package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository  extends JpaRepository<Faculty,Long> {
}
