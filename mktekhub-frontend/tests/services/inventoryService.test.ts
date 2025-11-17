import { describe, it, expect, beforeEach, afterEach } from "vitest";
import MockAdapter from "axios-mock-adapter";
import api from "../../src/services/api";
import { inventoryService } from "../../src/services/inventoryService";
import type { InventoryItem, InventoryItemRequest } from "../../src/types";

describe("inventoryService", () => {
  let mock: MockAdapter;

  const mockInventoryItem: InventoryItem = {
    id: 1,
    sku: "SKU001",
    name: "Test Item",
    description: "Test Description",
    category: "Electronics",
    brand: "TestBrand",
    quantity: 100,
    unitPrice: 29.99,
    volumePerUnit: 2.5,
    totalVolume: 250,
    reorderLevel: 20,
    warrantyEndDate: "2025-12-31",
    expirationDate: "2026-01-01",
    barcode: "123456789",
    warehouseId: 1,
    warehouseName: "Warehouse A",
    totalValue: 2999,
    isLowStock: false,
    isExpired: false,
    isWarrantyValid: true,
    createdAt: "2024-01-01T00:00:00",
    updatedAt: "2024-01-01T00:00:00",
  };

  beforeEach(() => {
    mock = new MockAdapter(api);
  });

  afterEach(() => {
    mock.reset();
  });

  describe("getAll", () => {
    it("should fetch all inventory items", async () => {
      const mockItems: InventoryItem[] = [mockInventoryItem];

      mock.onGet("/inventory").reply(200, mockItems);

      const result = await inventoryService.getAll();

      expect(result).toEqual(mockItems);
      expect(result).toHaveLength(1);
      expect(result[0].sku).toBe("SKU001");
    });

    it("should return empty array when no items exist", async () => {
      mock.onGet("/inventory").reply(200, []);

      const result = await inventoryService.getAll();

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });

    it("should handle errors", async () => {
      mock.onGet("/inventory").reply(500);

      await expect(inventoryService.getAll()).rejects.toThrow();
    });
  });

  describe("getById", () => {
    it("should fetch inventory item by id", async () => {
      mock.onGet("/inventory/1").reply(200, mockInventoryItem);

      const result = await inventoryService.getById(1);

      expect(result).toEqual(mockInventoryItem);
      expect(result.id).toBe(1);
    });

    it("should handle not found error", async () => {
      mock.onGet("/inventory/999").reply(404, {
        message: "Inventory item not found",
      });

      await expect(inventoryService.getById(999)).rejects.toThrow();
    });
  });

  describe("getBySku", () => {
    it("should fetch inventory item by SKU", async () => {
      mock.onGet("/inventory/sku/SKU001").reply(200, mockInventoryItem);

      const result = await inventoryService.getBySku("SKU001");

      expect(result).toEqual(mockInventoryItem);
      expect(result.sku).toBe("SKU001");
    });

    it("should handle not found error", async () => {
      mock.onGet("/inventory/sku/INVALID").reply(404);

      await expect(inventoryService.getBySku("INVALID")).rejects.toThrow();
    });
  });

  describe("getByWarehouse", () => {
    it("should fetch inventory items by warehouse id", async () => {
      const mockItems: InventoryItem[] = [mockInventoryItem];

      mock.onGet("/inventory/warehouse/1").reply(200, mockItems);

      const result = await inventoryService.getByWarehouse(1);

      expect(result).toEqual(mockItems);
      expect(result[0].warehouseId).toBe(1);
    });

    it("should return empty array for warehouse with no items", async () => {
      mock.onGet("/inventory/warehouse/2").reply(200, []);

      const result = await inventoryService.getByWarehouse(2);

      expect(result).toEqual([]);
    });
  });

  describe("getByCategory", () => {
    it("should fetch inventory items by category", async () => {
      const mockItems: InventoryItem[] = [mockInventoryItem];

      mock.onGet("/inventory/category/Electronics").reply(200, mockItems);

      const result = await inventoryService.getByCategory("Electronics");

      expect(result).toEqual(mockItems);
      expect(result[0].category).toBe("Electronics");
    });

    it("should handle special characters in category name", async () => {
      mock.onGet("/inventory/category/Food & Beverage").reply(200, []);

      const result = await inventoryService.getByCategory("Food & Beverage");

      expect(result).toEqual([]);
    });
  });

  describe("getLowStock", () => {
    it("should fetch low stock items", async () => {
      const lowStockItem: InventoryItem = {
        ...mockInventoryItem,
        quantity: 5,
        reorderLevel: 20,
        isLowStock: true,
      };

      mock.onGet("/inventory/alerts/low-stock").reply(200, [lowStockItem]);

      const result = await inventoryService.getLowStock();

      expect(result).toHaveLength(1);
      expect(result[0].isLowStock).toBe(true);
      expect(result[0].quantity).toBeLessThan(result[0].reorderLevel!);
    });

    it("should return empty array when no low stock items", async () => {
      mock.onGet("/inventory/alerts/low-stock").reply(200, []);

      const result = await inventoryService.getLowStock();

      expect(result).toEqual([]);
    });
  });

  describe("getExpired", () => {
    it("should fetch expired items", async () => {
      const expiredItem: InventoryItem = {
        ...mockInventoryItem,
        expirationDate: "2023-01-01",
        isExpired: true,
      };

      mock.onGet("/inventory/alerts/expired").reply(200, [expiredItem]);

      const result = await inventoryService.getExpired();

      expect(result).toHaveLength(1);
      expect(result[0].isExpired).toBe(true);
    });
  });

  describe("getExpiringSoon", () => {
    it("should fetch expiring soon items with default days", async () => {
      const expiringSoonItem: InventoryItem = {
        ...mockInventoryItem,
        expirationDate: "2024-12-31",
        isExpired: false,
      };

      mock
        .onGet("/inventory/alerts/expiring?days=30")
        .reply(200, [expiringSoonItem]);

      const result = await inventoryService.getExpiringSoon();

      expect(result).toHaveLength(1);
      expect(result[0].expirationDate).toBeDefined();
    });

    it("should fetch expiring soon items with custom days", async () => {
      mock.onGet("/inventory/alerts/expiring?days=7").reply(200, []);

      const result = await inventoryService.getExpiringSoon(7);

      expect(result).toEqual([]);
    });
  });

  describe("create", () => {
    it("should create a new inventory item", async () => {
      const newItemRequest: InventoryItemRequest = {
        sku: "SKU002",
        name: "New Item",
        description: "New Description",
        category: "Electronics",
        brand: "NewBrand",
        quantity: 50,
        unitPrice: 19.99,
        volumePerUnit: 1.5,
        reorderLevel: 10,
        warehouseId: 1,
        barcode: "987654321",
      };

      const createdItem: InventoryItem = {
        ...mockInventoryItem,
        ...newItemRequest,
        id: 2,
        warehouseName: "Warehouse A",
        totalVolume: 75,
        totalValue: 999.5,
      };

      mock.onPost("/inventory", newItemRequest).reply(201, createdItem);

      const result = await inventoryService.create(newItemRequest);

      expect(result).toEqual(createdItem);
      expect(result.id).toBe(2);
      expect(result.sku).toBe("SKU002");
    });

    it("should handle validation errors", async () => {
      const invalidRequest: InventoryItemRequest = {
        sku: "",
        name: "",
        quantity: -1,
        warehouseId: 0,
      };

      mock.onPost("/inventory").reply(400, {
        message: "Validation failed",
        validationErrors: {
          sku: "SKU is required",
          name: "Name is required",
          quantity: "Quantity must be positive",
        },
      });

      await expect(inventoryService.create(invalidRequest)).rejects.toThrow();
    });

    it("should handle duplicate SKU error", async () => {
      const duplicateRequest: InventoryItemRequest = {
        sku: "SKU001",
        name: "Duplicate Item",
        quantity: 10,
        warehouseId: 1,
      };

      mock.onPost("/inventory").reply(409, {
        message: "SKU already exists",
      });

      await expect(
        inventoryService.create(duplicateRequest),
      ).rejects.toThrow();
    });
  });

  describe("update", () => {
    it("should update an inventory item", async () => {
      const updateRequest: InventoryItemRequest = {
        ...mockInventoryItem,
        name: "Updated Item",
        quantity: 150,
      };

      const updatedItem: InventoryItem = {
        ...mockInventoryItem,
        name: "Updated Item",
        quantity: 150,
      };

      mock.onPut("/inventory/1", updateRequest).reply(200, updatedItem);

      const result = await inventoryService.update(1, updateRequest);

      expect(result).toEqual(updatedItem);
      expect(result.name).toBe("Updated Item");
      expect(result.quantity).toBe(150);
    });

    it("should handle not found error", async () => {
      const updateRequest: InventoryItemRequest = {
        sku: "SKU001",
        name: "Updated Item",
        quantity: 10,
        warehouseId: 1,
      };

      mock.onPut("/inventory/999").reply(404);

      await expect(
        inventoryService.update(999, updateRequest),
      ).rejects.toThrow();
    });
  });

  describe("delete", () => {
    it("should delete an inventory item", async () => {
      mock.onDelete("/inventory/1").reply(204);

      await expect(inventoryService.delete(1)).resolves.toBeUndefined();
    });

    it("should handle not found error", async () => {
      mock.onDelete("/inventory/999").reply(404);

      await expect(inventoryService.delete(999)).rejects.toThrow();
    });
  });

  describe("adjustQuantity", () => {
    it("should increase quantity", async () => {
      const adjustedItem: InventoryItem = {
        ...mockInventoryItem,
        quantity: 110,
      };

      mock
        .onPatch("/inventory/1/adjust", { quantityChange: 10 })
        .reply(200, adjustedItem);

      const result = await inventoryService.adjustQuantity(1, 10);

      expect(result.quantity).toBe(110);
    });

    it("should decrease quantity", async () => {
      const adjustedItem: InventoryItem = {
        ...mockInventoryItem,
        quantity: 90,
      };

      mock
        .onPatch("/inventory/1/adjust", { quantityChange: -10 })
        .reply(200, adjustedItem);

      const result = await inventoryService.adjustQuantity(1, -10);

      expect(result.quantity).toBe(90);
    });

    it("should handle insufficient quantity error", async () => {
      mock.onPatch("/inventory/1/adjust", { quantityChange: -200 }).reply(400, {
        message: "Insufficient quantity",
      });

      await expect(inventoryService.adjustQuantity(1, -200)).rejects.toThrow();
    });

    it("should handle not found error", async () => {
      mock.onPatch("/inventory/999/adjust").reply(404);

      await expect(
        inventoryService.adjustQuantity(999, 10),
      ).rejects.toThrow();
    });
  });
});
