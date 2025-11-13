import api from "./api";
import type { StockActivity } from "../types";

export const stockActivityService = {
  getAll: async (): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>("/stock-activities");
    return response.data;
  },

  getById: async (id: number): Promise<StockActivity> => {
    const response = await api.get<StockActivity>(`/stock-activities/${id}`);
    return response.data;
  },

  getByItemId: async (itemId: number): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/item/${itemId}`,
    );
    return response.data;
  },

  getByItemSku: async (sku: string): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/sku/${sku}`,
    );
    return response.data;
  },

  getByActivityType: async (type: string): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/type/${type}`,
    );
    return response.data;
  },

  getByUserId: async (userId: number): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/user/${userId}`,
    );
    return response.data;
  },

  getByUsername: async (username: string): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/user/username/${username}`,
    );
    return response.data;
  },

  getByWarehouse: async (warehouseId: number): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/warehouse/${warehouseId}`,
    );
    return response.data;
  },

  getByDateRange: async (
    startDate: string,
    endDate: string,
  ): Promise<StockActivity[]> => {
    const response = await api.get<StockActivity[]>(
      `/stock-activities/date-range`,
      {
        params: { startDate, endDate },
      },
    );
    return response.data;
  },
};
