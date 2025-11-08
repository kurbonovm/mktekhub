package com.mktekhub.inventory.config;

import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize default roles on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        createRoleIfNotExists("ADMIN", "Full system access with all privileges");
        createRoleIfNotExists("MANAGER", "Warehouse and inventory management access");
        createRoleIfNotExists("VIEWER", "Read-only access to view data");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name, description);
            roleRepository.save(role);
            System.out.println("Created role: " + name);
        }
    }
}
