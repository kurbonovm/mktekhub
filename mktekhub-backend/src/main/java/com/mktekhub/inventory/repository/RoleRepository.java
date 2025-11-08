package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * Provides database operations for role management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by name.
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role exists by name.
     */
    boolean existsByName(String name);
}
