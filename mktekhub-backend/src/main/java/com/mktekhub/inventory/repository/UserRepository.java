package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database operations for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by username.
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user exists by email.
     */
    boolean existsByEmail(String email);
}
