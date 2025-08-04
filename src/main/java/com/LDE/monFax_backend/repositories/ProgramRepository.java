package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program,Long> {
}
