package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.exception.InsufficientStockException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockTransferService stockTransferService;

    private Warehouse sourceWarehouse;
    private Warehouse destinationWarehouse;
    private InventoryItem sourceItem;
    private StockTransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        sourceWarehouse = new Warehouse();
        sourceWarehouse.setId(1L);
        sourceWarehouse.setName("Source Warehouse");
        sourceWarehouse.setMaxCapacity(new BigDecimal("10000"));
        sourceWarehouse.setCurrentCapacity(new BigDecimal("5000"));
        sourceWarehouse.setIsActive(true);

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setId(2L);
        destinationWarehouse.setName("Destination Warehouse");
        destinationWarehouse.setMaxCapacity(new BigDecimal("10000"));
        destinationWarehouse.setCurrentCapacity(new BigDecimal("3000"));
        destinationWarehouse.setIsActive(true);

        sourceItem = new InventoryItem();
        sourceItem.setId(1L);
        sourceItem.setSku("SKU001");
        sourceItem.setName("Test Item");
        sourceItem.setQuantity(100);
        sourceItem.setVolumePerUnit(new BigDecimal("10"));
        sourceItem.setWarehouse(sourceWarehouse);

        transferRequest = new StockTransferRequest();
        transferRequest.setItemSku("SKU001");
        transferRequest.setSourceWarehouseId(1L);
        transferRequest.setDestinationWarehouseId(2L);
        transferRequest.setQuantity(20);
    }

    @Test
    void transferStock_ThrowsException_WhenSameWarehouse() {
        transferRequest.setDestinationWarehouseId(1L);
        assertThrows(InvalidOperationException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenSourceWarehouseNotFound() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenDestinationWarehouseNotFound() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenSourceWarehouseNotActive() {
        sourceWarehouse.setIsActive(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        assertThrows(InvalidOperationException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenDestinationWarehouseNotActive() {
        destinationWarehouse.setIsActive(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        assertThrows(InvalidOperationException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenItemNotFoundInSource() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU001", 1L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    void transferStock_ThrowsException_WhenInsufficientStock() {
        transferRequest.setQuantity(150);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU001", 1L)).thenReturn(sourceItem);
        assertThrows(InsufficientStockException.class, () -> stockTransferService.transferStock(transferRequest));
    }
}
