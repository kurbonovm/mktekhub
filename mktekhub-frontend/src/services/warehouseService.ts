import api from "./api";
import type { Warehouse, WarehouseRequest } from "../types";

export const warehouseService = {
  getAll: async (): Promise<Warehouse[]> => {
    const response = await api.get<Warehouse[]>("/warehouses");
    return response.data;
  },

  getActive: async (): Promise<Warehouse[]> => {
    const response = await api.get<Warehouse[]>("/warehouses/active");
    return response.data;
  },

  getById: async (id: number): Promise<Warehouse> => {
    const response = await api.get<Warehouse>(`/warehouses/${id}`);
    return response.data;
  },

  getWithAlerts: async (): Promise<Warehouse[]> => {
    const response = await api.get<Warehouse[]>("/warehouses/alerts");
    return response.data;
  },

  create: async (data: WarehouseRequest): Promise<Warehouse> => {
    const response = await api.post<Warehouse>("/warehouses", data);
    return response.data;
  },

  update: async (id: number, data: WarehouseRequest): Promise<Warehouse> => {
    const response = await api.put<Warehouse>(`/warehouses/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/warehouses/${id}`);
  },

  permanentDelete: async (id: number): Promise<void> => {
    await api.delete(`/warehouses/${id}/permanent`);
  },
};
