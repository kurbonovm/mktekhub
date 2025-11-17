package com.mktekhub.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.RoleRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import com.mktekhub.inventory.security.JwtUtil;
import com.mktekhub.inventory.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WarehouseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        warehouseRepository.deleteAll();
        userRepository.deleteAll();

        // Create or find role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(adminRole));
        testUser = userRepository.save(testUser);

        // Generate JWT token
        UserDetailsImpl userDetails = new UserDetailsImpl(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        jwtToken = jwtUtil.generateToken(authentication);
    }

    @Test
    void getAllWarehouses_ReturnsEmptyList_WhenNoWarehouses() throws Exception {
        mockMvc.perform(get("/api/warehouses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllWarehouses_ReturnsWarehouses_WhenWarehousesExist() throws Exception {
        // Arrange
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        warehouse.setIsActive(true);
        warehouseRepository.save(warehouse);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Warehouse")))
                .andExpect(jsonPath("$[0].location", is("New York")))
                .andExpect(jsonPath("$[0].maxCapacity", is(10000.00)));
    }

    @Test
    void createWarehouse_Success() throws Exception {
        // Arrange
        WarehouseRequest request = new WarehouseRequest();
        request.setName("New Warehouse");
        request.setLocation("Boston");
        request.setMaxCapacity(new BigDecimal("15000.00"));
        request.setCapacityAlertThreshold(new BigDecimal("75.00"));

        // Act & Assert
        mockMvc.perform(post("/api/warehouses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Warehouse")))
                .andExpect(jsonPath("$.location", is("Boston")))
                .andExpect(jsonPath("$.maxCapacity", is(15000.00)))
                .andExpect(jsonPath("$.currentCapacity", is(0)))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    void createWarehouse_ReturnsBadRequest_WhenNameIsEmpty() throws Exception {
        // Arrange
        WarehouseRequest request = new WarehouseRequest();
        request.setName("");
        request.setLocation("Boston");
        request.setMaxCapacity(new BigDecimal("15000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/warehouses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWarehouse_ReturnsConflict_WhenNameAlreadyExists() throws Exception {
        // Arrange
        Warehouse existing = new Warehouse();
        existing.setName("Existing Warehouse");
        existing.setLocation("New York");
        existing.setMaxCapacity(new BigDecimal("10000.00"));
        existing.setCurrentCapacity(BigDecimal.ZERO);
        existing.setIsActive(true);
        warehouseRepository.save(existing);

        WarehouseRequest request = new WarehouseRequest();
        request.setName("Existing Warehouse");
        request.setLocation("Boston");
        request.setMaxCapacity(new BigDecimal("15000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/warehouses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getWarehouseById_ReturnsWarehouse_WhenExists() throws Exception {
        // Arrange
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        warehouse.setIsActive(true);
        Warehouse saved = warehouseRepository.save(warehouse);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Warehouse")));
    }

    @Test
    void getWarehouseById_ReturnsNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/warehouses/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWarehouse_Success() throws Exception {
        // Arrange
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Original Name");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        warehouse.setIsActive(true);
        Warehouse saved = warehouseRepository.save(warehouse);

        WarehouseRequest updateRequest = new WarehouseRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setLocation("Boston");
        updateRequest.setMaxCapacity(new BigDecimal("15000.00"));

        // Act & Assert
        mockMvc.perform(put("/api/warehouses/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.location", is("Boston")))
                .andExpect(jsonPath("$.maxCapacity", is(15000.00)));
    }

    @Test
    void deleteWarehouse_Success_WhenEmpty() throws Exception {
        // Arrange
        Warehouse warehouse = new Warehouse();
        warehouse.setName("To Delete");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        warehouse.setIsActive(true);
        Warehouse saved = warehouseRepository.save(warehouse);

        // Act & Assert
        mockMvc.perform(delete("/api/warehouses/" + saved.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void createWarehouse_ReturnsUnauthorized_WhenNoToken() throws Exception {
        // Arrange
        WarehouseRequest request = new WarehouseRequest();
        request.setName("New Warehouse");
        request.setLocation("Boston");
        request.setMaxCapacity(new BigDecimal("15000.00"));

        // Act & Assert
        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getActiveWarehouses_ReturnsOnlyActiveWarehouses() throws Exception {
        // Arrange
        Warehouse active = new Warehouse();
        active.setName("Active Warehouse");
        active.setLocation("New York");
        active.setMaxCapacity(new BigDecimal("10000.00"));
        active.setCurrentCapacity(BigDecimal.ZERO);
        active.setIsActive(true);
        warehouseRepository.save(active);

        Warehouse inactive = new Warehouse();
        inactive.setName("Inactive Warehouse");
        inactive.setLocation("Boston");
        inactive.setMaxCapacity(new BigDecimal("10000.00"));
        inactive.setCurrentCapacity(BigDecimal.ZERO);
        inactive.setIsActive(false);
        warehouseRepository.save(inactive);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses/active")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Active Warehouse")));
    }
}
