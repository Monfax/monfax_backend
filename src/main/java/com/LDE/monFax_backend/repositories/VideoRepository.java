package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video,Long> {
    long count();
}
