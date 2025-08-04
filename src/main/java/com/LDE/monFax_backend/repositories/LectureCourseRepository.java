package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.LectureCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureCourseRepository  extends JpaRepository<LectureCourse,Long> {
    long count();
}
