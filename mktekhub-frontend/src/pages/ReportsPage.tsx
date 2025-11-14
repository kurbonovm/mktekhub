import { useState } from "react";
import api from "../services/api";

export const ReportsPage = () => {
  const [isDownloading, setIsDownloading] = useState<string | null>(null);

  const downloadReport = async (endpoint: string, reportName: string) => {
    setIsDownloading(reportName);
    try {
      const response = await api.get(`/reports/${endpoint}`, {
        responseType: "blob",
      });

      // Create blob link to download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;

      // Get filename from Content-Disposition header or use default
      const contentDisposition = response.headers["content-disposition"];
      let filename = `${reportName}.csv`;
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
      console.error(`Error downloading ${reportName}:`, error);
      alert(`Failed to download ${reportName}. Please try again.`);
    } finally {
      setIsDownloading(null);
    }
  };

  const reports = [
    {
      id: "inventory",
      name: "Inventory Export",
      description: "Complete inventory list with all item details",
      endpoint: "export/inventory",
      icon: "📦",
      category: "Exports",
    },
    {
      id: "warehouses",
      name: "Warehouses Export",
      description: "All warehouse information and capacity data",
      endpoint: "export/warehouses",
      icon: "🏢",
      category: "Exports",
    },
    {
      id: "activities",
      name: "Stock Activities Export",
      description: "Complete history of stock movements and changes",
      endpoint: "export/activities",
      icon: "📝",
      category: "Exports",
    },
    {
      id: "valuation",
      name: "Stock Valuation Report",
      description: "Total value of inventory by item and category",
      endpoint: "valuation",
      icon: "💰",
      category: "Financial",
    },
    {
      id: "low-stock",
      name: "Low Stock Report",
      description: "Items below reorder level or out of stock",
      endpoint: "low-stock",
      icon: "⚠️",
      category: "Alerts",
    },
    {
      id: "warehouse-utilization",
      name: "Warehouse Utilization Report",
      description: "Capacity usage and availability by warehouse",
      endpoint: "warehouse-utilization",
      icon: "📊",
      category: "Analytics",
    },
    {
      id: "stock-movement",
      name: "Stock Movement Report",
      description: "Detailed history of transfers and adjustments",
      endpoint: "stock-movement",
      icon: "🔄",
      category: "Analytics",
    },
    {
      id: "inventory-by-category",
      name: "Inventory by Category",
      description: "Summary of items grouped by category",
      endpoint: "inventory-by-category",
      icon: "📂",
      category: "Analytics",
    },
  ];

  const categories = ["Exports", "Financial", "Alerts", "Analytics"];

  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-6 lg:p-8">
      <div className="mx-auto max-w-7xl">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Reports & Exports
          </h1>
          <p className="mt-2 text-sm text-gray-600">
            Generate and download various reports and data exports
          </p>
        </div>

        {/* Info Card */}
        <div className="mb-8 rounded-lg bg-blue-50 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg
                className="h-5 w-5 text-blue-400"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700">
                All reports are generated in CSV format and can be opened in
                Excel, Google Sheets, or any spreadsheet application.
              </p>
            </div>
          </div>
        </div>

        {/* Reports by Category */}
        {categories.map((category) => {
          const categoryReports = reports.filter(
            (r) => r.category === category,
          );
          if (categoryReports.length === 0) return null;

          return (
            <div key={category} className="mb-8">
              <h2 className="mb-4 text-xl font-semibold text-gray-800">
                {category}
              </h2>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {categoryReports.map((report) => (
                  <div
                    key={report.id}
                    className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm transition-shadow hover:shadow-md"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="mb-2 flex items-center">
                          <span className="mr-2 text-2xl">{report.icon}</span>
                          <h3 className="text-lg font-semibold text-gray-900">
                            {report.name}
                          </h3>
                        </div>
                        <p className="mb-4 text-sm text-gray-600">
                          {report.description}
                        </p>
                      </div>
                    </div>
                    <button
                      onClick={() =>
                        downloadReport(report.endpoint, report.name)
                      }
                      disabled={isDownloading === report.name}
                      className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                    >
                      {isDownloading === report.name ? (
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
                          Downloading...
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
                          Download CSV
                        </span>
                      )}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          );
        })}

        {/* Help Section */}
        <div className="mt-8 rounded-lg border border-gray-200 bg-white p-6">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">
            Report Descriptions
          </h3>
          <div className="space-y-3 text-sm text-gray-600">
            <div>
              <strong className="text-gray-900">Inventory Export:</strong>{" "}
              Complete list of all items including SKU, name, category,
              quantity, price, and warehouse location.
            </div>
            <div>
              <strong className="text-gray-900">Stock Valuation:</strong>{" "}
              Financial report showing the total value of inventory calculated
              from unit prices and quantities.
            </div>
            <div>
              <strong className="text-gray-900">Low Stock Report:</strong>{" "}
              Identifies items that are below their reorder level or completely
              out of stock, requiring attention.
            </div>
            <div>
              <strong className="text-gray-900">Warehouse Utilization:</strong>{" "}
              Shows capacity usage percentages for each warehouse to help
              optimize space.
            </div>
            <div>
              <strong className="text-gray-900">Stock Movement:</strong>{" "}
              Detailed log of all inventory transfers, adjustments, and changes
              over time.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
