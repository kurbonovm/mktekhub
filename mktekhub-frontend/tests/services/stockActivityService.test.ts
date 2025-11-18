import { describe, it, expect, beforeEach, afterEach } from "vitest";
import MockAdapter from "axios-mock-adapter";
import api from "../../src/services/api";
import { stockActivityService } from "../../src/services/stockActivityService";
import type { StockActivity } from "../../src/types";

describe("stockActivityService", () => {
  let mock: MockAdapter;

  const mockStockActivity: StockActivity = {
    id: 1,
    itemSku: "SKU001",
    itemName: "Test Item",
    activityType: "RECEIVE",
    quantityChange: 50,
    previousQuantity: 100,
    newQuantity: 150,
    timestamp: "2024-01-15T10:30:00",
    performedBy: "testuser",
    notes: "Received new shipment",
  };

  beforeEach(() => {
    mock = new MockAdapter(api);
  });

  afterEach(() => {
    mock.reset();
  });

  describe("getAll", () => {
    it("should fetch all stock activities", async () => {
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock.onGet("/stock-activities").reply(200, mockActivities);

      const result = await stockActivityService.getAll();

      expect(result).toEqual(mockActivities);
      expect(result).toHaveLength(1);
      expect(result[0].id).toBe(1);
    });

    it("should return empty array when no activities exist", async () => {
      mock.onGet("/stock-activities").reply(200, []);

      const result = await stockActivityService.getAll();

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });

    it("should handle errors", async () => {
      mock.onGet("/stock-activities").reply(500);

      await expect(stockActivityService.getAll()).rejects.toThrow();
    });
  });

  describe("getById", () => {
    it("should fetch stock activity by id", async () => {
      mock.onGet("/stock-activities/1").reply(200, mockStockActivity);

      const result = await stockActivityService.getById(1);

      expect(result).toEqual(mockStockActivity);
      expect(result.id).toBe(1);
      expect(result.itemSku).toBe("SKU001");
    });

    it("should handle not found error", async () => {
      mock.onGet("/stock-activities/999").reply(404, {
        message: "Stock activity not found",
      });

      await expect(stockActivityService.getById(999)).rejects.toThrow();
    });

    it("should handle network errors", async () => {
      mock.onGet("/stock-activities/1").networkError();

      await expect(stockActivityService.getById(1)).rejects.toThrow();
    });
  });

  describe("getByItemId", () => {
    it("should fetch stock activities by item id", async () => {
      const mockActivities: StockActivity[] = [
        mockStockActivity,
        {
          ...mockStockActivity,
          id: 2,
          activityType: "SALE",
          quantityChange: -10,
          newQuantity: 140,
        },
      ];

      mock.onGet("/stock-activities/item/1").reply(200, mockActivities);

      const result = await stockActivityService.getByItemId(1);

      expect(result).toEqual(mockActivities);
      expect(result).toHaveLength(2);
    });

    it("should return empty array for item with no activities", async () => {
      mock.onGet("/stock-activities/item/999").reply(200, []);

      const result = await stockActivityService.getByItemId(999);

      expect(result).toEqual([]);
    });

    it("should handle errors", async () => {
      mock.onGet("/stock-activities/item/1").reply(500);

      await expect(stockActivityService.getByItemId(1)).rejects.toThrow();
    });
  });

  describe("getByItemSku", () => {
    it("should fetch stock activities by item SKU", async () => {
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock.onGet("/stock-activities/sku/SKU001").reply(200, mockActivities);

      const result = await stockActivityService.getByItemSku("SKU001");

      expect(result).toEqual(mockActivities);
      expect(result[0].itemSku).toBe("SKU001");
    });

    it("should return empty array for non-existent SKU", async () => {
      mock.onGet("/stock-activities/sku/INVALID").reply(200, []);

      const result = await stockActivityService.getByItemSku("INVALID");

      expect(result).toEqual([]);
    });

    it("should handle special characters in SKU", async () => {
      const specialSku = "SKU-001/A";
      mock.onGet(`/stock-activities/sku/${specialSku}`).reply(200, []);

      const result = await stockActivityService.getByItemSku(specialSku);

      expect(result).toEqual([]);
    });
  });

  describe("getByActivityType", () => {
    it("should fetch stock activities by RECEIVE type", async () => {
      const receiveActivities: StockActivity[] = [
        { ...mockStockActivity, activityType: "RECEIVE" },
      ];

      mock
        .onGet("/stock-activities/type/RECEIVE")
        .reply(200, receiveActivities);

      const result = await stockActivityService.getByActivityType("RECEIVE");

      expect(result).toEqual(receiveActivities);
      expect(result[0].activityType).toBe("RECEIVE");
    });

    it("should fetch stock activities by TRANSFER type", async () => {
      const transferActivity: StockActivity = {
        ...mockStockActivity,
        activityType: "TRANSFER",
        sourceWarehouseName: "Warehouse A",
        destinationWarehouseName: "Warehouse B",
      };

      mock
        .onGet("/stock-activities/type/TRANSFER")
        .reply(200, [transferActivity]);

      const result = await stockActivityService.getByActivityType("TRANSFER");

      expect(result[0].activityType).toBe("TRANSFER");
      expect(result[0].sourceWarehouseName).toBe("Warehouse A");
      expect(result[0].destinationWarehouseName).toBe("Warehouse B");
    });

    it("should fetch stock activities by SALE type", async () => {
      const saleActivity: StockActivity = {
        ...mockStockActivity,
        activityType: "SALE",
        quantityChange: -25,
      };

      mock.onGet("/stock-activities/type/SALE").reply(200, [saleActivity]);

      const result = await stockActivityService.getByActivityType("SALE");

      expect(result[0].activityType).toBe("SALE");
      expect(result[0].quantityChange).toBeLessThan(0);
    });

    it("should return empty array for type with no activities", async () => {
      mock.onGet("/stock-activities/type/DELETE").reply(200, []);

      const result = await stockActivityService.getByActivityType("DELETE");

      expect(result).toEqual([]);
    });
  });

  describe("getByUserId", () => {
    it("should fetch stock activities by user id", async () => {
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock.onGet("/stock-activities/user/1").reply(200, mockActivities);

      const result = await stockActivityService.getByUserId(1);

      expect(result).toEqual(mockActivities);
      expect(result[0].performedBy).toBe("testuser");
    });

    it("should return empty array for user with no activities", async () => {
      mock.onGet("/stock-activities/user/999").reply(200, []);

      const result = await stockActivityService.getByUserId(999);

      expect(result).toEqual([]);
    });

    it("should handle errors", async () => {
      mock.onGet("/stock-activities/user/1").reply(500);

      await expect(stockActivityService.getByUserId(1)).rejects.toThrow();
    });
  });

  describe("getByUsername", () => {
    it("should fetch stock activities by username", async () => {
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock
        .onGet("/stock-activities/user/username/testuser")
        .reply(200, mockActivities);

      const result = await stockActivityService.getByUsername("testuser");

      expect(result).toEqual(mockActivities);
      expect(result[0].performedBy).toBe("testuser");
    });

    it("should return empty array for username with no activities", async () => {
      mock.onGet("/stock-activities/user/username/noactivity").reply(200, []);

      const result = await stockActivityService.getByUsername("noactivity");

      expect(result).toEqual([]);
    });

    it("should handle special characters in username", async () => {
      const username = "user.name@domain";
      mock.onGet(`/stock-activities/user/username/${username}`).reply(200, []);

      const result = await stockActivityService.getByUsername(username);

      expect(result).toEqual([]);
    });
  });

  describe("getByWarehouse", () => {
    it("should fetch stock activities by warehouse id", async () => {
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock.onGet("/stock-activities/warehouse/1").reply(200, mockActivities);

      const result = await stockActivityService.getByWarehouse(1);

      expect(result).toEqual(mockActivities);
      expect(result).toHaveLength(1);
    });

    it("should return empty array for warehouse with no activities", async () => {
      mock.onGet("/stock-activities/warehouse/999").reply(200, []);

      const result = await stockActivityService.getByWarehouse(999);

      expect(result).toEqual([]);
    });

    it("should handle errors", async () => {
      mock.onGet("/stock-activities/warehouse/1").reply(500);

      await expect(stockActivityService.getByWarehouse(1)).rejects.toThrow();
    });
  });

  describe("getByDateRange", () => {
    it("should fetch stock activities within date range", async () => {
      const startDate = "2024-01-01";
      const endDate = "2024-01-31";
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock
        .onGet("/stock-activities/date-range", {
          params: { startDate, endDate },
        })
        .reply(200, mockActivities);

      const result = await stockActivityService.getByDateRange(
        startDate,
        endDate,
      );

      expect(result).toEqual(mockActivities);
      expect(result).toHaveLength(1);
    });

    it("should return empty array when no activities in date range", async () => {
      const startDate = "2023-01-01";
      const endDate = "2023-01-31";

      mock
        .onGet("/stock-activities/date-range", {
          params: { startDate, endDate },
        })
        .reply(200, []);

      const result = await stockActivityService.getByDateRange(
        startDate,
        endDate,
      );

      expect(result).toEqual([]);
    });

    it("should handle invalid date range", async () => {
      const startDate = "2024-01-31";
      const endDate = "2024-01-01"; // End before start

      mock
        .onGet("/stock-activities/date-range", {
          params: { startDate, endDate },
        })
        .reply(400, {
          message: "Invalid date range: end date must be after start date",
        });

      await expect(
        stockActivityService.getByDateRange(startDate, endDate),
      ).rejects.toThrow();
    });

    it("should handle same start and end date", async () => {
      const date = "2024-01-15";
      const mockActivities: StockActivity[] = [mockStockActivity];

      mock
        .onGet("/stock-activities/date-range", {
          params: { startDate: date, endDate: date },
        })
        .reply(200, mockActivities);

      const result = await stockActivityService.getByDateRange(date, date);

      expect(result).toEqual(mockActivities);
    });

    it("should handle network errors", async () => {
      mock.onGet("/stock-activities/date-range").networkError();

      await expect(
        stockActivityService.getByDateRange("2024-01-01", "2024-01-31"),
      ).rejects.toThrow();
    });
  });
});
