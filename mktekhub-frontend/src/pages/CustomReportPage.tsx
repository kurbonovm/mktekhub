import { useState, useEffect, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import api from "../services/api";
import { type Warehouse } from "../types";

type ReportType = "stock-activity" | "inventory-valuation";

// Minimal interface for inventory items used in this component
interface InventoryItemBasic {
  category?: string;
  brand?: string;
}

export const CustomReportPage = () => {
  const [reportType, setReportType] = useState<ReportType>("stock-activity");
  const [isDownloading, setIsDownloading] = useState(false);

  // Filter state
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [category, setCategory] = useState("");
  const [brand, setBrand] = useState("");
  const [warehouseId, setWarehouseId] = useState("");
  const [activityType, setActivityType] = useState("");

  // Fetch warehouses
  const { data: warehouses } = useQuery<Warehouse[]>({
    queryKey: ["warehouses"],
    queryFn: async () => {
      const response = await api.get("/warehouses");
      return response.data;
    },
  });

  // Fetch inventory items to get unique categories and brands
  const { data: inventoryItems } = useQuery<InventoryItemBasic[]>({
    queryKey: ["inventory"],
    queryFn: async () => {
      const response = await api.get("/inventory");
      return response.data;
    },
  });

  // Get unique categories and brands
  const categories = useMemo(() => {
    if (!inventoryItems) return [];
    const uniqueCategories = new Set(
      inventoryItems
        .map((item) => item.category)
        .filter((cat) => cat && cat.trim() !== ""),
    );
    return Array.from(uniqueCategories).sort();
  }, [inventoryItems]);

  const brands = useMemo(() => {
    if (!inventoryItems) return [];
    const uniqueBrands = new Set(
      inventoryItems
        .map((item) => item.brand)
        .filter((brand) => brand && brand.trim() !== ""),
    );
    return Array.from(uniqueBrands).sort();
  }, [inventoryItems]);

  // Reset filters when report type changes
  useEffect(() => {
    setStartDate("");
    setEndDate("");
    setCategory("");
    setBrand("");
    setWarehouseId("");
    setActivityType("");
  }, [reportType]);

  const downloadReport = async () => {
    setIsDownloading(true);
    try {
      // Build query parameters
      const params = new URLSearchParams();

      if (reportType === "stock-activity") {
        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);
        if (activityType) params.append("activityType", activityType);
      }

      if (category) params.append("category", category);
      if (brand) params.append("brand", brand);
      if (warehouseId) params.append("warehouseId", warehouseId);

      const endpoint =
        reportType === "stock-activity"
          ? "/reports/custom/stock-activity"
          : "/reports/custom/inventory-valuation";

      const response = await api.get(
        `${endpoint}${params.toString() ? `?${params.toString()}` : ""}`,
        { responseType: "blob" },
      );

      // Create blob link to download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;

      // Get filename from Content-Disposition header or use default
      const contentDisposition = response.headers["content-disposition"];
      let filename = `custom_report_${reportType}.csv`;
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?(.+)"?/i);
        if (filenameMatch) {
          filename = filenameMatch[1];
        }
      }

      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Error downloading report:", error);
      alert("Failed to download report. Please try again.");
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-6 lg:p-8">
      <div className="mx-auto max-w-4xl">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Custom Reports</h1>
          <p className="mt-2 text-sm text-gray-600">
            Generate filtered reports based on your criteria
          </p>
        </div>

        {/* Report Configuration Card */}
        <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
          {/* Report Type Selection */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700">
              Report Type
            </label>
            <select
              value={reportType}
              onChange={(e) => setReportType(e.target.value as ReportType)}
              className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
            >
              <option value="stock-activity">Stock Activity Report</option>
              <option value="inventory-valuation">
                Inventory Valuation Report
              </option>
            </select>
          </div>

          {/* Filters Section */}
          <div className="space-y-6">
            <h2 className="text-lg font-semibold text-gray-900">Filters</h2>

            {/* Date Range - Only for Stock Activity */}
            {reportType === "stock-activity" && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Start Date
                  </label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    End Date
                  </label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                  />
                </div>
              </div>
            )}

            {/* Activity Type - Only for Stock Activity */}
            {reportType === "stock-activity" && (
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  Activity Type
                </label>
                <select
                  value={activityType}
                  onChange={(e) => setActivityType(e.target.value)}
                  className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
                >
                  <option value="">All Activity Types</option>
                  <option value="ADD">Add</option>
                  <option value="REMOVE">Remove</option>
                  <option value="TRANSFER">Transfer</option>
                  <option value="ADJUSTMENT">Adjustment</option>
                </select>
              </div>
            )}

            {/* Category Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
              >
                <option value="">All Categories</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            {/* Brand Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Brand
              </label>
              <select
                value={brand}
                onChange={(e) => setBrand(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
              >
                <option value="">All Brands</option>
                {brands.map((b) => (
                  <option key={b} value={b}>
                    {b}
                  </option>
                ))}
              </select>
            </div>

            {/* Warehouse Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Warehouse
              </label>
              <select
                value={warehouseId}
                onChange={(e) => setWarehouseId(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-base focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 sm:text-sm"
              >
                <option value="">All Warehouses</option>
                {warehouses?.map((warehouse) => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name} - {warehouse.location}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Download Button */}
          <div className="mt-8">
            <button
              onClick={downloadReport}
              disabled={isDownloading}
              className="w-full rounded-md bg-blue-600 px-4 py-3 text-base font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-400 sm:text-sm"
            >
              {isDownloading ? (
                <span className="flex items-center justify-center">
                  <svg
                    className="mr-2 h-4 w-4 animate-spin text-white"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  Generating Report...
                </span>
              ) : (
                <span className="flex items-center justify-center">
                  <svg
                    className="mr-2 h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  Download CSV Report
                </span>
              )}
            </button>
          </div>
        </div>

        {/* Help Section */}
        <div className="mt-6 rounded-lg border border-gray-200 bg-white p-6">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">
            About Custom Reports
          </h3>
          <div className="space-y-3 text-sm text-gray-600">
            <div>
              <strong className="text-gray-900">Stock Activity Report:</strong>{" "}
              Shows detailed history of stock movements with filters for date
              range, activity type, category, brand, and warehouse. Use this to
              track specific inventory changes over time.
            </div>
            <div>
              <strong className="text-gray-900">
                Inventory Valuation Report:
              </strong>{" "}
              Displays the total value of inventory with filters for category,
              brand, and warehouse. Use this for financial analysis and
              inventory worth assessment.
            </div>
            <div className="mt-4 rounded-md bg-blue-50 p-3">
              <p className="text-sm text-blue-700">
                <strong>Tip:</strong> Leave filters empty to include all data.
                Apply specific filters to narrow down your report results.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
