import api from "./api";
import type { DashboardSummary, CombinedAlerts } from "../types";

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const response = await api.get<DashboardSummary>("/dashboard/summary");
    return response.data;
  },

  getWarehouseSummary: async (): Promise<
    DashboardSummary["warehouseSummary"]
  > => {
    const response = await api.get<DashboardSummary["warehouseSummary"]>(
      "/dashboard/warehouse-summary",
    );
    return response.data;
  },

  getInventorySummary: async (): Promise<
    DashboardSummary["inventorySummary"]
  > => {
    const response = await api.get<DashboardSummary["inventorySummary"]>(
      "/dashboard/inventory-summary",
    );
    return response.data;
  },

  getAlertsSummary: async (): Promise<DashboardSummary["alertsSummary"]> => {
    const response = await api.get<DashboardSummary["alertsSummary"]>(
      "/dashboard/alerts-summary",
    );
    return response.data;
  },

  getAllAlerts: async (expiringDays: number = 30): Promise<CombinedAlerts> => {
    const response = await api.get<CombinedAlerts>(
      `/alerts/all?expiringDays=${expiringDays}`,
    );
    return response.data;
  },
};
