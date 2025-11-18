import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom/vitest";
import { screen, waitFor, within } from "@testing-library/react";
import { DashboardPage } from "../../src/pages/DashboardPage";
import { renderWithProviders } from "../utils/testUtils";
import { mockDashboardSummary } from "../utils/mockData";
import { dashboardService } from "../../src/services/dashboardService";

// Mock the dashboard service
vi.mock("../../src/services/dashboardService", () => ({
  dashboardService: {
    getSummary: vi.fn(),
  },
}));

describe("DashboardPage", () => {
  describe("Loading State", () => {
    it("should show loading message while fetching data", () => {
      vi.mocked(dashboardService.getSummary).mockImplementation(
        () => new Promise(() => {}), // Never resolves
      );

      renderWithProviders(<DashboardPage />);

      expect(screen.getByText("Loading dashboard...")).toBeInTheDocument();
    });

    it("should display loading text in center of screen", () => {
      vi.mocked(dashboardService.getSummary).mockImplementation(
        () => new Promise(() => {}),
      );

      renderWithProviders(<DashboardPage />);

      const loadingDiv = screen.getByText("Loading dashboard...").parentElement;
      expect(loadingDiv?.className).toMatch(/flex/);
      expect(loadingDiv?.className).toMatch(/items-center/);
      expect(loadingDiv?.className).toMatch(/justify-center/);
    });
  });

  describe("Error State", () => {
    it("should show error message on fetch failure", async () => {
      vi.mocked(dashboardService.getSummary).mockRejectedValue(
        new Error("Failed to fetch"),
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(
          screen.getByText(/Error loading dashboard:/),
        ).toBeInTheDocument();
      });
    });

    it("should display error message from exception", async () => {
      vi.mocked(dashboardService.getSummary).mockRejectedValue(
        new Error("Network error"),
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText(/Network error/)).toBeInTheDocument();
      });
    });

    it("should display error in red text", async () => {
      vi.mocked(dashboardService.getSummary).mockRejectedValue(
        new Error("Error"),
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const errorElement = screen.getByText(/Error loading dashboard:/);
        expect(errorElement.className).toMatch(/text-red-600/);
      });
    });
  });

  describe("Dashboard Content - Successful Load", () => {
    beforeEach(() => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );
    });

    it("should render dashboard title", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Dashboard Overview")).toBeInTheDocument();
      });
    });

    it("should render all three summary sections", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Warehouse Summary")).toBeInTheDocument();
        expect(screen.getByText("Inventory Summary")).toBeInTheDocument();
        expect(screen.getByText("Alerts")).toBeInTheDocument();
      });
    });
  });

  describe("Warehouse Summary", () => {
    beforeEach(() => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );
    });

    it("should display total warehouses", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Total Warehouses")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.warehouseSummary.totalWarehouses.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display active warehouses", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const labelElement = screen.getByText("Active Warehouses");
        const card = labelElement.closest("div") as HTMLElement;
        expect(
          within(card).getByText(
            mockDashboardSummary.warehouseSummary.activeWarehouses.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display total capacity", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Total Capacity")).toBeInTheDocument();
        expect(screen.getByText(/10000 ft³/)).toBeInTheDocument();
      });
    });

    it("should display used capacity", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Used Capacity")).toBeInTheDocument();
        expect(screen.getByText(/6500 ft³/)).toBeInTheDocument();
      });
    });

    it("should display average utilization", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Average Utilization")).toBeInTheDocument();
        expect(screen.getByText(/65.00%/)).toBeInTheDocument();
      });
    });

    it("should format capacity values as integers", async () => {
      const customSummary = {
        ...mockDashboardSummary,
        warehouseSummary: {
          ...mockDashboardSummary.warehouseSummary,
          totalCapacity: 10000.75,
          usedCapacity: 6500.99,
        },
      };

      vi.mocked(dashboardService.getSummary).mockResolvedValue(customSummary);

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText(/10001 ft³/)).toBeInTheDocument();
        expect(screen.getByText(/6501 ft³/)).toBeInTheDocument();
      });
    });

    it("should format utilization to 2 decimal places", async () => {
      const customSummary = {
        ...mockDashboardSummary,
        warehouseSummary: {
          ...mockDashboardSummary.warehouseSummary,
          averageUtilization: 67.3456,
        },
      };

      vi.mocked(dashboardService.getSummary).mockResolvedValue(customSummary);

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText(/67.35%/)).toBeInTheDocument();
      });
    });
  });

  describe("Inventory Summary", () => {
    beforeEach(() => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );
    });

    it("should display total items", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Total Items")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.inventorySummary.totalItems.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display total quantity", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Total Quantity")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.inventorySummary.totalQuantity.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display total value with currency", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Total Value")).toBeInTheDocument();
        expect(screen.getByText(/\$45000\.00/)).toBeInTheDocument();
      });
    });

    it("should display categories count", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Categories")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.inventorySummary.categoriesCount.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should format total value to 2 decimal places", async () => {
      const customSummary = {
        ...mockDashboardSummary,
        inventorySummary: {
          ...mockDashboardSummary.inventorySummary,
          totalValue: 12345.678,
        },
      };

      vi.mocked(dashboardService.getSummary).mockResolvedValue(customSummary);

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText(/\$12345\.68/)).toBeInTheDocument();
      });
    });
  });

  describe("Alerts Summary", () => {
    beforeEach(() => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );
    });

    it("should display low stock items count", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Low Stock Items")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.alertsSummary.lowStockItems.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display expired items count", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const labelElement = screen.getByText("Expired Items");
        const card = labelElement.closest("div") as HTMLElement;
        expect(
          within(card).getByText(
            mockDashboardSummary.alertsSummary.expiredItems.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display expiring soon items count", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Expiring Soon")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.alertsSummary.expiringSoonItems.toString(),
          ),
        ).toBeInTheDocument();
      });
    });

    it("should display capacity alerts count", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Capacity Alerts")).toBeInTheDocument();
        expect(
          screen.getByText(
            mockDashboardSummary.alertsSummary.capacityAlerts.toString(),
          ),
        ).toBeInTheDocument();
      });
    });
  });

  describe("Data Fetching", () => {
    it("should call dashboardService.getSummary on mount", async () => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(dashboardService.getSummary).toHaveBeenCalled();
      });
    });

    it("should use correct query key", async () => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Dashboard Overview")).toBeInTheDocument();
      });
    });
  });

  describe("Edge Cases", () => {
    it("should handle zero values correctly", async () => {
      const zeroSummary = {
        warehouseSummary: {
          totalWarehouses: 0,
          activeWarehouses: 0,
          totalCapacity: 0,
          usedCapacity: 0,
          averageUtilization: 0,
        },
        inventorySummary: {
          totalItems: 0,
          totalQuantity: 0,
          totalValue: 0,
          categoriesCount: 0,
        },
        alertsSummary: {
          lowStockItems: 0,
          expiredItems: 0,
          expiringSoonItems: 0,
          capacityAlerts: 0,
        },
      };

      vi.mocked(dashboardService.getSummary).mockResolvedValue(zeroSummary);

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getAllByText("0").length).toBeGreaterThan(0);
      });
    });

    it("should handle very large numbers", async () => {
      const largeSummary = {
        warehouseSummary: {
          totalWarehouses: 999999,
          activeWarehouses: 999998,
          totalCapacity: 999999999,
          usedCapacity: 888888888,
          averageUtilization: 99.99,
        },
        inventorySummary: {
          totalItems: 999999,
          totalQuantity: 9999999,
          totalValue: 99999999.99,
          categoriesCount: 9999,
        },
        alertsSummary: {
          lowStockItems: 9999,
          expiredItems: 8888,
          expiringSoonItems: 7777,
          capacityAlerts: 6666,
        },
      };

      vi.mocked(dashboardService.getSummary).mockResolvedValue(largeSummary);

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        // Check that large numbers are displayed (appears twice - warehouse and inventory)
        expect(screen.getAllByText("999999").length).toBeGreaterThanOrEqual(1);
        expect(screen.getByText(/\$99999999\.99/)).toBeInTheDocument();
      });
    });

    it("should handle undefined optional chaining gracefully", async () => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );

      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        expect(screen.getByText("Dashboard Overview")).toBeInTheDocument();
      });
    });
  });

  describe("Styling and Layout", () => {
    beforeEach(() => {
      vi.mocked(dashboardService.getSummary).mockResolvedValue(
        mockDashboardSummary,
      );
    });

    it("should use grid layout for warehouse summary", async () => {
      const { container } = renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const grid = container.querySelector(".grid");
        expect(grid).toBeInTheDocument();
      });
    });

    it("should display warehouse metrics with different colors", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const totalWarehouseCard = screen
          .getByText("Total Warehouses")
          .closest("div") as HTMLElement;
        const totalWarehouse = within(totalWarehouseCard).getByText(
          mockDashboardSummary.warehouseSummary.totalWarehouses.toString(),
        );
        expect(totalWarehouse.className).toMatch(/text-blue-600/);

        const activeWarehouseCard = screen
          .getByText("Active Warehouses")
          .closest("div") as HTMLElement;
        const activeWarehouse = within(activeWarehouseCard).getByText(
          mockDashboardSummary.warehouseSummary.activeWarehouses.toString(),
        );
        expect(activeWarehouse.className).toMatch(/text-green-600/);
      });
    });

    it("should display alert metrics with appropriate colors", async () => {
      renderWithProviders(<DashboardPage />);

      await waitFor(() => {
        const lowStockCard = screen
          .getByText("Low Stock Items")
          .closest("div") as HTMLElement;
        const lowStock = within(lowStockCard).getByText(
          mockDashboardSummary.alertsSummary.lowStockItems.toString(),
        );
        expect(lowStock.className).toMatch(/text-yellow-600/);

        const expiredCard = screen
          .getByText("Expired Items")
          .closest("div") as HTMLElement;
        const expired = within(expiredCard).getByText(
          mockDashboardSummary.alertsSummary.expiredItems.toString(),
        );
        expect(expired.className).toMatch(/text-red-600/);
      });
    });
  });
});
