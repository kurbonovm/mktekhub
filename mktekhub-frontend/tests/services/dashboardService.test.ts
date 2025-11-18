import { describe, it, expect, beforeEach, afterEach } from "vitest";
import MockAdapter from "axios-mock-adapter";
import api from "../../src/services/api";
import { dashboardService } from "../../src/services/dashboardService";
import type { DashboardSummary, CombinedAlerts } from "../../src/types";

describe("dashboardService", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
  });

  afterEach(() => {
    mock.reset();
  });

  describe("getSummary", () => {
    it("should fetch complete dashboard summary", async () => {
      const mockSummary: DashboardSummary = {
        warehouseSummary: {
          totalWarehouses: 5,
          activeWarehouses: 4,
          totalCapacity: 100000,
          usedCapacity: 60000,
          averageUtilization: 60,
          warehousesWithAlerts: 2,
        },
        inventorySummary: {
          totalItems: 150,
          totalQuantity: 5000,
          totalValue: 250000,
          uniqueSkus: 120,
          categoriesCount: 10,
        },
        alertsSummary: {
          lowStockItems: 15,
          expiredItems: 3,
          expiringSoonItems: 8,
          capacityAlerts: 2,
        },
      };

      mock.onGet("/dashboard/summary").reply(200, mockSummary);

      const result = await dashboardService.getSummary();

      expect(result).toEqual(mockSummary);
      expect(result.warehouseSummary.totalWarehouses).toBe(5);
      expect(result.inventorySummary.totalItems).toBe(150);
      expect(result.alertsSummary.lowStockItems).toBe(15);
    });

    it("should handle errors when fetching summary", async () => {
      mock.onGet("/dashboard/summary").reply(500, {
        message: "Internal server error",
      });

      await expect(dashboardService.getSummary()).rejects.toThrow();
    });

    it("should handle network errors", async () => {
      mock.onGet("/dashboard/summary").networkError();

      await expect(dashboardService.getSummary()).rejects.toThrow();
    });
  });

  describe("getWarehouseSummary", () => {
    it("should fetch warehouse summary", async () => {
      const mockWarehouseSummary = {
        totalWarehouses: 5,
        activeWarehouses: 4,
        totalCapacity: 100000,
        usedCapacity: 60000,
        averageUtilization: 60,
        warehousesWithAlerts: 2,
      };

      mock
        .onGet("/dashboard/warehouse-summary")
        .reply(200, mockWarehouseSummary);

      const result = await dashboardService.getWarehouseSummary();

      expect(result).toEqual(mockWarehouseSummary);
      expect(result.totalWarehouses).toBe(5);
      expect(result.averageUtilization).toBe(60);
    });

    it("should handle errors", async () => {
      mock.onGet("/dashboard/warehouse-summary").reply(500);

      await expect(dashboardService.getWarehouseSummary()).rejects.toThrow();
    });
  });

  describe("getInventorySummary", () => {
    it("should fetch inventory summary", async () => {
      const mockInventorySummary = {
        totalItems: 150,
        totalQuantity: 5000,
        totalValue: 250000,
        uniqueSkus: 120,
        categoriesCount: 10,
      };

      mock
        .onGet("/dashboard/inventory-summary")
        .reply(200, mockInventorySummary);

      const result = await dashboardService.getInventorySummary();

      expect(result).toEqual(mockInventorySummary);
      expect(result.totalItems).toBe(150);
      expect(result.totalValue).toBe(250000);
    });

    it("should handle errors", async () => {
      mock.onGet("/dashboard/inventory-summary").reply(404);

      await expect(dashboardService.getInventorySummary()).rejects.toThrow();
    });
  });

  describe("getAlertsSummary", () => {
    it("should fetch alerts summary", async () => {
      const mockAlertsSummary = {
        lowStockItems: 15,
        expiredItems: 3,
        expiringSoonItems: 8,
        capacityAlerts: 2,
      };

      mock.onGet("/dashboard/alerts-summary").reply(200, mockAlertsSummary);

      const result = await dashboardService.getAlertsSummary();

      expect(result).toEqual(mockAlertsSummary);
      expect(result.lowStockItems).toBe(15);
      expect(result.expiredItems).toBe(3);
    });

    it("should handle errors", async () => {
      mock.onGet("/dashboard/alerts-summary").reply(500);

      await expect(dashboardService.getAlertsSummary()).rejects.toThrow();
    });
  });

  describe("getAllAlerts", () => {
    it("should fetch all alerts with default expiring days", async () => {
      const mockAlerts: CombinedAlerts = {
        lowStockItems: [
          {
            id: 1,
            sku: "SKU001",
            name: "Item 1",
            quantity: 5,
            reorderLevel: 10,
            warehouseId: 1,
            warehouseName: "Warehouse A",
            isLowStock: true,
            isExpired: false,
            isWarrantyValid: true,
            createdAt: "2024-01-01T00:00:00",
            updatedAt: "2024-01-01T00:00:00",
          },
        ],
        expiredItems: [],
        expiringSoonItems: [],
        capacityAlerts: [],
      };

      mock.onGet("/alerts/all?expiringDays=30").reply(200, mockAlerts);

      const result = await dashboardService.getAllAlerts();

      expect(result).toEqual(mockAlerts);
      expect(result.lowStockItems).toHaveLength(1);
    });

    it("should fetch all alerts with custom expiring days", async () => {
      const mockAlerts: CombinedAlerts = {
        lowStockItems: [],
        expiredItems: [],
        expiringSoonItems: [
          {
            id: 2,
            sku: "SKU002",
            name: "Item 2",
            quantity: 50,
            expirationDate: "2024-12-31",
            warehouseId: 1,
            warehouseName: "Warehouse A",
            isLowStock: false,
            isExpired: false,
            isWarrantyValid: true,
            createdAt: "2024-01-01T00:00:00",
            updatedAt: "2024-01-01T00:00:00",
          },
        ],
        capacityAlerts: [],
      };

      mock.onGet("/alerts/all?expiringDays=7").reply(200, mockAlerts);

      const result = await dashboardService.getAllAlerts(7);

      expect(result).toEqual(mockAlerts);
      expect(result.expiringSoonItems).toHaveLength(1);
    });

    it("should handle errors when fetching all alerts", async () => {
      mock.onGet("/alerts/all?expiringDays=30").reply(500);

      await expect(dashboardService.getAllAlerts()).rejects.toThrow();
    });

    it("should handle unauthorized access", async () => {
      mock.onGet("/alerts/all?expiringDays=30").reply(401);

      await expect(dashboardService.getAllAlerts()).rejects.toThrow();
    });
  });
});
