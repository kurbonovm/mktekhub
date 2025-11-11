import { useQuery } from "@tanstack/react-query";
import { dashboardService } from "../services/dashboardService";

export const DashboardPage = () => {
  const {
    data: summary,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["dashboard-summary"],
    queryFn: () => dashboardService.getSummary(),
  });

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl">Loading dashboard...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl text-red-600">
          Error loading dashboard: {(error as Error).message}
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <h1 className="mb-8 text-3xl font-bold text-gray-900">
        Dashboard Overview
      </h1>

      {/* Warehouse Summary */}
      <div className="mb-8">
        <h2 className="mb-4 text-2xl font-semibold text-gray-800">
          Warehouse Summary
        </h2>
        <div className="grid gap-4 md:grid-cols-3">
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Total Warehouses</p>
            <p className="text-3xl font-bold text-blue-600">
              {summary?.warehouseSummary.totalWarehouses}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Active Warehouses</p>
            <p className="text-3xl font-bold text-green-600">
              {summary?.warehouseSummary.activeWarehouses}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Average Utilization</p>
            <p className="text-3xl font-bold text-purple-600">
              {summary?.warehouseSummary.averageUtilization.toFixed(2)}%
            </p>
          </div>
        </div>
      </div>

      {/* Inventory Summary */}
      <div className="mb-8">
        <h2 className="mb-4 text-2xl font-semibold text-gray-800">
          Inventory Summary
        </h2>
        <div className="grid gap-4 md:grid-cols-4">
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Total Items</p>
            <p className="text-3xl font-bold text-blue-600">
              {summary?.inventorySummary.totalItems}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Total Quantity</p>
            <p className="text-3xl font-bold text-green-600">
              {summary?.inventorySummary.totalQuantity}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Total Value</p>
            <p className="text-3xl font-bold text-purple-600">
              ${summary?.inventorySummary.totalValue.toFixed(2)}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Categories</p>
            <p className="text-3xl font-bold text-orange-600">
              {summary?.inventorySummary.categoriesCount}
            </p>
          </div>
        </div>
      </div>

      {/* Alerts Summary */}
      <div>
        <h2 className="mb-4 text-2xl font-semibold text-gray-800">Alerts</h2>
        <div className="grid gap-4 md:grid-cols-4">
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Low Stock Items</p>
            <p className="text-3xl font-bold text-yellow-600">
              {summary?.alertsSummary.lowStockItems}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Expired Items</p>
            <p className="text-3xl font-bold text-red-600">
              {summary?.alertsSummary.expiredItems}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Expiring Soon</p>
            <p className="text-3xl font-bold text-orange-600">
              {summary?.alertsSummary.expiringSoonItems}
            </p>
          </div>
          <div className="rounded-lg bg-white p-6 shadow">
            <p className="text-sm text-gray-600">Capacity Alerts</p>
            <p className="text-3xl font-bold text-purple-600">
              {summary?.alertsSummary.capacityAlerts}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
