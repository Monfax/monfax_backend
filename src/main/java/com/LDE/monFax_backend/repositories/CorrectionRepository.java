package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.Correction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CorrectionRepository  extends JpaRepository<Correction,Long> {
}
