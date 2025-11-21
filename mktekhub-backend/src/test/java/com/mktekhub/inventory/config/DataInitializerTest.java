/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Role;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.RoleRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for DataInitializer. Tests initialization of default roles, warehouses,
 * and sample inventory items.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

  @Mock private RoleRepository roleRepository;

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private InventoryItemRepository inventoryItemRepository;

  @InjectMocks private DataInitializer dataInitializer;

  @BeforeEach
  void setUp() {
    // Default setup - repositories return empty results
  }

  // ==================== ROLE CREATION TESTS ====================

  @Test
  @DisplayName("Run - Should create ADMIN role if not exists")
  void run_ShouldCreateAdminRole() throws Exception {
    // Arrange
    when(roleRepository.existsByName("ADMIN")).thenReturn(false);
    when(roleRepository.existsByName("MANAGER")).thenReturn(true);
    when(roleRepository.existsByName("VIEWER")).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(1L); // Skip warehouse seeding

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository, atLeastOnce()).save(roleCaptor.capture());

    // Find the ADMIN role in captured roles
    List<Role> capturedRoles = roleCaptor.getAllValues();
    Role adminRole =
        capturedRoles.stream().filter(r -> "ADMIN".equals(r.getName())).findFirst().orElse(null);

    assertNotNull(adminRole);
    assertEquals("ADMIN", adminRole.getName());
    assertEquals("Full system access with all privileges", adminRole.getDescription());
  }

  @Test
  @DisplayName("Run - Should create MANAGER role if not exists")
  void run_ShouldCreateManagerRole() throws Exception {
    // Arrange
    when(roleRepository.existsByName("ADMIN")).thenReturn(true);
    when(roleRepository.existsByName("MANAGER")).thenReturn(false);
    when(roleRepository.existsByName("VIEWER")).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(1L);

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository, atLeastOnce()).save(roleCaptor.capture());

    List<Role> capturedRoles = roleCaptor.getAllValues();
    Role managerRole =
        capturedRoles.stream().filter(r -> "MANAGER".equals(r.getName())).findFirst().orElse(null);

    assertNotNull(managerRole);
    assertEquals("MANAGER", managerRole.getName());
    assertEquals("Warehouse and inventory management access", managerRole.getDescription());
  }

  @Test
  @DisplayName("Run - Should create VIEWER role if not exists")
  void run_ShouldCreateViewerRole() throws Exception {
    // Arrange
    when(roleRepository.existsByName("ADMIN")).thenReturn(true);
    when(roleRepository.existsByName("MANAGER")).thenReturn(true);
    when(roleRepository.existsByName("VIEWER")).thenReturn(false);
    when(warehouseRepository.count()).thenReturn(1L);

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository, atLeastOnce()).save(roleCaptor.capture());

    List<Role> capturedRoles = roleCaptor.getAllValues();
    Role viewerRole =
        capturedRoles.stream().filter(r -> "VIEWER".equals(r.getName())).findFirst().orElse(null);

    assertNotNull(viewerRole);
    assertEquals("VIEWER", viewerRole.getName());
    assertEquals("Read-only access to view data", viewerRole.getDescription());
  }

  @Test
  @DisplayName("Run - Should create all three roles when none exist")
  void run_ShouldCreateAllRolesWhenNoneExist() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(false);
    when(warehouseRepository.count()).thenReturn(1L);

    // Act
    dataInitializer.run();

    // Assert
    verify(roleRepository, times(3)).save(any(Role.class));
    verify(roleRepository).existsByName("ADMIN");
    verify(roleRepository).existsByName("MANAGER");
    verify(roleRepository).existsByName("VIEWER");
  }

  @Test
  @DisplayName("Run - Should not create roles that already exist")
  void run_ShouldNotCreateExistingRoles() throws Exception {
    // Arrange
    when(roleRepository.existsByName("ADMIN")).thenReturn(true);
    when(roleRepository.existsByName("MANAGER")).thenReturn(true);
    when(roleRepository.existsByName("VIEWER")).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(1L);

    // Act
    dataInitializer.run();

    // Assert
    verify(roleRepository, never()).save(any(Role.class));
  }

  // ==================== WAREHOUSE SEEDING TESTS ====================

  @Test
  @DisplayName("Run - Should seed warehouses when count is zero")
  void run_ShouldSeedWarehousesWhenCountIsZero() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    // Mock warehouse saves
    Warehouse mainWarehouse = new Warehouse();
    mainWarehouse.setName("Main Warehouse");
    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(mainWarehouse));

    Warehouse westCoast = new Warehouse();
    westCoast.setName("West Coast Distribution Center");
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(westCoast));

    Warehouse eastCoast = new Warehouse();
    eastCoast.setName("East Coast Hub");
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(eastCoast));

    // Act
    dataInitializer.run();

    // Assert - Should save 3 warehouses
    verify(warehouseRepository, atLeast(3)).save(any(Warehouse.class));
  }

  @Test
  @DisplayName("Run - Should not seed warehouses when count is greater than zero")
  void run_ShouldNotSeedWarehousesWhenCountIsNonZero() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(5L);

    // Act
    dataInitializer.run();

    // Assert - Should not save any warehouses or inventory items
    verify(warehouseRepository, never()).save(any(Warehouse.class));
    verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
  }

  @Test
  @DisplayName("Run - Should create Main Warehouse with correct properties")
  void run_ShouldCreateMainWarehouseWithCorrectProperties() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    Warehouse savedWarehouse = new Warehouse();
    savedWarehouse.setName("Main Warehouse");
    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(savedWarehouse));
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(new Warehouse()));
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(new Warehouse()));

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository, atLeast(3)).save(warehouseCaptor.capture());

    // Find Main Warehouse in captured warehouses
    Warehouse mainWarehouse =
        warehouseCaptor.getAllValues().stream()
            .filter(w -> "Main Warehouse".equals(w.getName()))
            .findFirst()
            .orElse(null);

    assertNotNull(mainWarehouse);
    assertEquals("Main Warehouse", mainWarehouse.getName());
    assertEquals("123 Main St, New York, NY 10001", mainWarehouse.getLocation());
    assertNotNull(mainWarehouse.getMaxCapacity());
    assertNotNull(mainWarehouse.getCapacityAlertThreshold());
  }

  @Test
  @DisplayName("Run - Should create West Coast Distribution Center")
  void run_ShouldCreateWestCoastDistributionCenter() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(new Warehouse()));
    Warehouse westCoast = new Warehouse();
    westCoast.setName("West Coast Distribution Center");
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(westCoast));
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(new Warehouse()));

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository, atLeast(3)).save(warehouseCaptor.capture());

    Warehouse warehouse =
        warehouseCaptor.getAllValues().stream()
            .filter(w -> "West Coast Distribution Center".equals(w.getName()))
            .findFirst()
            .orElse(null);

    assertNotNull(warehouse);
    assertEquals("West Coast Distribution Center", warehouse.getName());
    assertEquals("456 Pacific Ave, Los Angeles, CA 90001", warehouse.getLocation());
  }

  @Test
  @DisplayName("Run - Should create East Coast Hub")
  void run_ShouldCreateEastCoastHub() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(new Warehouse()));
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(new Warehouse()));
    Warehouse eastCoast = new Warehouse();
    eastCoast.setName("East Coast Hub");
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(eastCoast));

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository, atLeast(3)).save(warehouseCaptor.capture());

    Warehouse warehouse =
        warehouseCaptor.getAllValues().stream()
            .filter(w -> "East Coast Hub".equals(w.getName()))
            .findFirst()
            .orElse(null);

    assertNotNull(warehouse);
    assertEquals("East Coast Hub", warehouse.getName());
    assertEquals("789 Atlantic Blvd, Boston, MA 02101", warehouse.getLocation());
  }

  // ==================== INVENTORY SEEDING TESTS ====================

  @Test
  @DisplayName("Run - Should seed inventory items when warehouses are seeded")
  void run_ShouldSeedInventoryItemsWhenWarehousesSeeded() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    Warehouse mainWarehouse = new Warehouse();
    mainWarehouse.setName("Main Warehouse");
    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(mainWarehouse));

    Warehouse westCoast = new Warehouse();
    westCoast.setName("West Coast Distribution Center");
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(westCoast));

    Warehouse eastCoast = new Warehouse();
    eastCoast.setName("East Coast Hub");
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(eastCoast));

    // Act
    dataInitializer.run();

    // Assert - Should save multiple inventory items (30+)
    verify(inventoryItemRepository, atLeast(30)).save(any(InventoryItem.class));
  }

  @Test
  @DisplayName("Run - Should create inventory items with various categories")
  void run_ShouldCreateInventoryItemsWithVariousCategories() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    Warehouse warehouse = new Warehouse();
    warehouse.setName("Main Warehouse");
    when(warehouseRepository.findByName(anyString())).thenReturn(Optional.of(warehouse));

    // Act
    dataInitializer.run();

    // Assert
    ArgumentCaptor<InventoryItem> itemCaptor = ArgumentCaptor.forClass(InventoryItem.class);
    verify(inventoryItemRepository, atLeast(1)).save(itemCaptor.capture());

    List<InventoryItem> items = itemCaptor.getAllValues();
    assertFalse(items.isEmpty());

    // Verify some items were created
    assertTrue(
        items.stream()
            .anyMatch(item -> item.getSku() != null && item.getSku().startsWith("ELEC-")));
  }

  // ==================== INTEGRATION TESTS ====================

  @Test
  @DisplayName("Integration - Full initialization with empty database")
  void integration_FullInitializationWithEmptyDatabase() throws Exception {
    // Arrange - Empty database
    when(roleRepository.existsByName(anyString())).thenReturn(false);
    when(warehouseRepository.count()).thenReturn(0L);

    Warehouse mainWarehouse = new Warehouse();
    mainWarehouse.setName("Main Warehouse");
    when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(mainWarehouse));

    Warehouse westCoast = new Warehouse();
    westCoast.setName("West Coast Distribution Center");
    when(warehouseRepository.findByName("West Coast Distribution Center"))
        .thenReturn(Optional.of(westCoast));

    Warehouse eastCoast = new Warehouse();
    eastCoast.setName("East Coast Hub");
    when(warehouseRepository.findByName("East Coast Hub")).thenReturn(Optional.of(eastCoast));

    // Act
    dataInitializer.run();

    // Assert - All initialization should happen
    verify(roleRepository, times(3)).save(any(Role.class)); // 3 roles
    verify(warehouseRepository, atLeast(3)).save(any(Warehouse.class)); // 3 warehouses
    verify(inventoryItemRepository, atLeast(30)).save(any(InventoryItem.class)); // 30+ items
  }

  @Test
  @DisplayName("Integration - Partial initialization with existing roles")
  void integration_PartialInitializationWithExistingRoles() throws Exception {
    // Arrange - Roles exist, but no warehouses
    when(roleRepository.existsByName("ADMIN")).thenReturn(true);
    when(roleRepository.existsByName("MANAGER")).thenReturn(true);
    when(roleRepository.existsByName("VIEWER")).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(0L);

    Warehouse warehouse = new Warehouse();
    when(warehouseRepository.findByName(anyString())).thenReturn(Optional.of(warehouse));

    // Act
    dataInitializer.run();

    // Assert
    verify(roleRepository, never()).save(any(Role.class)); // No roles created
    verify(warehouseRepository, atLeast(3)).save(any(Warehouse.class)); // Warehouses created
    verify(inventoryItemRepository, atLeast(30)).save(any(InventoryItem.class)); // Items created
  }

  @Test
  @DisplayName("Integration - No seeding with populated database")
  void integration_NoSeedingWithPopulatedDatabase() throws Exception {
    // Arrange - Everything exists
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(5L);

    // Act
    dataInitializer.run();

    // Assert - Nothing should be created
    verify(roleRepository, never()).save(any(Role.class));
    verify(warehouseRepository, never()).save(any(Warehouse.class));
    verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
  }

  // ==================== EDGE CASES ====================

  @Test
  @DisplayName("Edge Case - Should handle warehouse count of exactly 1")
  void edgeCase_WarehouseCountExactlyOne() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(1L);

    // Act
    dataInitializer.run();

    // Assert - Should not seed
    verify(warehouseRepository, never()).save(any(Warehouse.class));
    verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
  }

  @Test
  @DisplayName("Edge Case - Should always check and create roles regardless of warehouse count")
  void edgeCase_AlwaysCheckRolesRegardlessOfWarehouseCount() throws Exception {
    // Arrange
    when(roleRepository.existsByName("ADMIN")).thenReturn(false);
    when(roleRepository.existsByName("MANAGER")).thenReturn(true);
    when(roleRepository.existsByName("VIEWER")).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(100L); // Many warehouses exist

    // Act
    dataInitializer.run();

    // Assert - ADMIN role should still be created
    verify(roleRepository).save(any(Role.class));
    verify(roleRepository).existsByName("ADMIN");
    verify(roleRepository).existsByName("MANAGER");
    verify(roleRepository).existsByName("VIEWER");
  }

  @Test
  @DisplayName("Edge Case - Run method can be called multiple times safely")
  void edgeCase_RunMethodIdempotent() throws Exception {
    // Arrange
    when(roleRepository.existsByName(anyString())).thenReturn(true);
    when(warehouseRepository.count()).thenReturn(3L);

    // Act - Call run multiple times
    dataInitializer.run();
    dataInitializer.run();
    dataInitializer.run();

    // Assert - Should not create duplicates
    verify(roleRepository, never()).save(any(Role.class));
    verify(warehouseRepository, never()).save(any(Warehouse.class));
    verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
  }
}
