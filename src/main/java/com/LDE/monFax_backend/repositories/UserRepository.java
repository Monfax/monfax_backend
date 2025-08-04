package com.LDE.monFax_backend.repositories;

import com.LDE.monFax_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT COUNT(u) FROM users u WHERE u.createdAt >= :dateLimit")
    long countUsersRegisteredSince(@Param("dateLimit") LocalDateTime dateLimit);
    List<User> findTop5ByOrderByLastLoginDesc();
}
