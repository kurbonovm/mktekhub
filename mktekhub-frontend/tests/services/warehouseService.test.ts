import { describe, it, expect, beforeEach, afterEach } from "vitest";
import MockAdapter from "axios-mock-adapter";
import api from "../../src/services/api";
import { warehouseService } from "../../src/services/warehouseService";
import type { Warehouse, WarehouseRequest } from "../../src/types";

describe("warehouseService", () => {
  let mock: MockAdapter;

  const mockWarehouse: Warehouse = {
    id: 1,
    name: "Warehouse A",
    location: "New York, NY",
    maxCapacity: 10000,
    currentCapacity: 6000,
    capacityAlertThreshold: 80,
    utilizationPercentage: 60,
    isAlertTriggered: false,
    isActive: true,
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
    it("should fetch all warehouses", async () => {
      const mockWarehouses: Warehouse[] = [mockWarehouse];

      mock.onGet("/warehouses").reply(200, mockWarehouses);

      const result = await warehouseService.getAll();

      expect(result).toEqual(mockWarehouses);
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe("Warehouse A");
    });

    it("should return empty array when no warehouses exist", async () => {
      mock.onGet("/warehouses").reply(200, []);

      const result = await warehouseService.getAll();

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });

    it("should handle errors", async () => {
      mock.onGet("/warehouses").reply(500);

      await expect(warehouseService.getAll()).rejects.toThrow();
    });
  });

  describe("getActive", () => {
    it("should fetch only active warehouses", async () => {
      const activeWarehouses: Warehouse[] = [
        mockWarehouse,
        { ...mockWarehouse, id: 2, name: "Warehouse B", isActive: true },
      ];

      mock.onGet("/warehouses/active").reply(200, activeWarehouses);

      const result = await warehouseService.getActive();

      expect(result).toEqual(activeWarehouses);
      expect(result.every((w) => w.isActive)).toBe(true);
    });

    it("should return empty array when no active warehouses", async () => {
      mock.onGet("/warehouses/active").reply(200, []);

      const result = await warehouseService.getActive();

      expect(result).toEqual([]);
    });

    it("should handle errors", async () => {
      mock.onGet("/warehouses/active").reply(500);

      await expect(warehouseService.getActive()).rejects.toThrow();
    });
  });

  describe("getById", () => {
    it("should fetch warehouse by id", async () => {
      mock.onGet("/warehouses/1").reply(200, mockWarehouse);

      const result = await warehouseService.getById(1);

      expect(result).toEqual(mockWarehouse);
      expect(result.id).toBe(1);
      expect(result.name).toBe("Warehouse A");
    });

    it("should handle not found error", async () => {
      mock.onGet("/warehouses/999").reply(404, {
        message: "Warehouse not found",
      });

      await expect(warehouseService.getById(999)).rejects.toThrow();
    });

    it("should handle network errors", async () => {
      mock.onGet("/warehouses/1").networkError();

      await expect(warehouseService.getById(1)).rejects.toThrow();
    });
  });

  describe("getWithAlerts", () => {
    it("should fetch warehouses with capacity alerts", async () => {
      const warehouseWithAlert: Warehouse = {
        ...mockWarehouse,
        currentCapacity: 8500,
        utilizationPercentage: 85,
        isAlertTriggered: true,
      };

      mock.onGet("/warehouses/alerts").reply(200, [warehouseWithAlert]);

      const result = await warehouseService.getWithAlerts();

      expect(result).toHaveLength(1);
      expect(result[0].isAlertTriggered).toBe(true);
      expect(result[0].utilizationPercentage).toBeGreaterThan(
        result[0].capacityAlertThreshold,
      );
    });

    it("should return empty array when no alerts", async () => {
      mock.onGet("/warehouses/alerts").reply(200, []);

      const result = await warehouseService.getWithAlerts();

      expect(result).toEqual([]);
    });

    it("should handle errors", async () => {
      mock.onGet("/warehouses/alerts").reply(500);

      await expect(warehouseService.getWithAlerts()).rejects.toThrow();
    });
  });

  describe("create", () => {
    it("should create a new warehouse", async () => {
      const newWarehouseRequest: WarehouseRequest = {
        name: "Warehouse C",
        location: "Los Angeles, CA",
        maxCapacity: 15000,
        capacityAlertThreshold: 75,
      };

      const createdWarehouse: Warehouse = {
        id: 3,
        name: "Warehouse C",
        location: "Los Angeles, CA",
        maxCapacity: 15000,
        currentCapacity: 0,
        capacityAlertThreshold: 75,
        utilizationPercentage: 0,
        isAlertTriggered: false,
        isActive: true,
        createdAt: "2024-01-15T00:00:00",
        updatedAt: "2024-01-15T00:00:00",
      };

      mock
        .onPost("/warehouses", newWarehouseRequest)
        .reply(201, createdWarehouse);

      const result = await warehouseService.create(newWarehouseRequest);

      expect(result).toEqual(createdWarehouse);
      expect(result.id).toBe(3);
      expect(result.name).toBe("Warehouse C");
      expect(result.currentCapacity).toBe(0);
    });

    it("should create warehouse with default threshold when not provided", async () => {
      const newWarehouseRequest: WarehouseRequest = {
        name: "Warehouse D",
        location: "Chicago, IL",
        maxCapacity: 12000,
      };

      const createdWarehouse: Warehouse = {
        id: 4,
        name: "Warehouse D",
        location: "Chicago, IL",
        maxCapacity: 12000,
        currentCapacity: 0,
        capacityAlertThreshold: 80, // Default value
        utilizationPercentage: 0,
        isAlertTriggered: false,
        isActive: true,
        createdAt: "2024-01-15T00:00:00",
        updatedAt: "2024-01-15T00:00:00",
      };

      mock
        .onPost("/warehouses", newWarehouseRequest)
        .reply(201, createdWarehouse);

      const result = await warehouseService.create(newWarehouseRequest);

      expect(result.capacityAlertThreshold).toBe(80);
    });

    it("should handle validation errors", async () => {
      const invalidRequest: WarehouseRequest = {
        name: "",
        location: "",
        maxCapacity: -1,
      };

      mock.onPost("/warehouses").reply(400, {
        message: "Validation failed",
        validationErrors: {
          name: "Name is required",
          location: "Location is required",
          maxCapacity: "Max capacity must be positive",
        },
      });

      await expect(warehouseService.create(invalidRequest)).rejects.toThrow();
    });

    it("should handle duplicate name error", async () => {
      const duplicateRequest: WarehouseRequest = {
        name: "Warehouse A",
        location: "New York, NY",
        maxCapacity: 10000,
      };

      mock.onPost("/warehouses").reply(409, {
        message: "Warehouse name already exists",
      });

      await expect(warehouseService.create(duplicateRequest)).rejects.toThrow();
    });
  });

  describe("update", () => {
    it("should update a warehouse", async () => {
      const updateRequest: WarehouseRequest = {
        name: "Warehouse A Updated",
        location: "New York, NY",
        maxCapacity: 12000,
        capacityAlertThreshold: 85,
      };

      const updatedWarehouse: Warehouse = {
        ...mockWarehouse,
        name: "Warehouse A Updated",
        maxCapacity: 12000,
        capacityAlertThreshold: 85,
        updatedAt: "2024-01-20T00:00:00",
      };

      mock.onPut("/warehouses/1", updateRequest).reply(200, updatedWarehouse);

      const result = await warehouseService.update(1, updateRequest);

      expect(result).toEqual(updatedWarehouse);
      expect(result.name).toBe("Warehouse A Updated");
      expect(result.maxCapacity).toBe(12000);
    });

    it("should handle not found error", async () => {
      const updateRequest: WarehouseRequest = {
        name: "Updated Name",
        location: "Updated Location",
        maxCapacity: 10000,
      };

      mock.onPut("/warehouses/999").reply(404);

      await expect(
        warehouseService.update(999, updateRequest),
      ).rejects.toThrow();
    });

    it("should handle validation errors", async () => {
      const invalidRequest: WarehouseRequest = {
        name: "",
        location: "",
        maxCapacity: 0,
      };

      mock.onPut("/warehouses/1").reply(400, {
        message: "Validation failed",
      });

      await expect(
        warehouseService.update(1, invalidRequest),
      ).rejects.toThrow();
    });
  });

  describe("delete", () => {
    it("should soft delete a warehouse", async () => {
      mock.onDelete("/warehouses/1").reply(204);

      await expect(warehouseService.delete(1)).resolves.toBeUndefined();
    });

    it("should handle not found error", async () => {
      mock.onDelete("/warehouses/999").reply(404, {
        message: "Warehouse not found",
      });

      await expect(warehouseService.delete(999)).rejects.toThrow();
    });

    it("should handle constraint violations", async () => {
      mock.onDelete("/warehouses/1").reply(409, {
        message: "Cannot delete warehouse with existing inventory",
      });

      await expect(warehouseService.delete(1)).rejects.toThrow();
    });
  });

  describe("permanentDelete", () => {
    it("should permanently delete a warehouse", async () => {
      mock.onDelete("/warehouses/1/permanent").reply(204);

      await expect(
        warehouseService.permanentDelete(1),
      ).resolves.toBeUndefined();
    });

    it("should handle not found error", async () => {
      mock.onDelete("/warehouses/999/permanent").reply(404);

      await expect(warehouseService.permanentDelete(999)).rejects.toThrow();
    });

    it("should handle constraint violations", async () => {
      mock.onDelete("/warehouses/1/permanent").reply(409, {
        message: "Cannot permanently delete warehouse with existing inventory",
      });

      await expect(warehouseService.permanentDelete(1)).rejects.toThrow();
    });

    it("should handle unauthorized access", async () => {
      mock.onDelete("/warehouses/1/permanent").reply(403, {
        message: "Insufficient permissions",
      });

      await expect(warehouseService.permanentDelete(1)).rejects.toThrow();
    });
  });
});
