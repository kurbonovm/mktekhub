import { describe, it, expect, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { InventoryFilters } from "../../../src/components/common/InventoryFilters";
import { defaultFilters } from "../../../src/types/filters";
import type { Warehouse } from "../../../src/types";

const mockWarehouses: Warehouse[] = [
  { id: 1, name: "Main Warehouse", location: "New York", capacity: 1000 },
  { id: 2, name: "West Coast", location: "California", capacity: 800 },
  { id: 3, name: "East Coast", location: "Boston", capacity: 600 },
];

const mockCategories = ["Electronics", "Furniture", "Clothing"];
const mockBrands = ["BrandA", "BrandB", "BrandC"];

describe("InventoryFilters", () => {
  const defaultProps = {
    filters: defaultFilters,
    onFilterChange: vi.fn(),
    onReset: vi.fn(),
    warehouses: mockWarehouses,
    categories: mockCategories,
    brands: mockBrands,
    isExpanded: true,
    onToggle: vi.fn(),
  };

  describe("Rendering", () => {
    it("should render filter header", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Filters")).toBeInTheDocument();
    });

    it("should show active filter count when filters are applied", () => {
      const filtersWithActive = {
        ...defaultFilters,
        category: "Electronics",
        brand: "BrandA",
      };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithActive} />,
      );

      expect(screen.getByText("2 active")).toBeInTheDocument();
    });

    it("should not show active count when no filters are applied", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.queryByText(/active/)).not.toBeInTheDocument();
    });

    it("should show Clear all button when filters are active", () => {
      const filtersWithActive = {
        ...defaultFilters,
        category: "Electronics",
      };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithActive} />,
      );

      expect(screen.getByText("Clear all")).toBeInTheDocument();
    });

    it("should not show Clear all button when no filters are active", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.queryByText("Clear all")).not.toBeInTheDocument();
    });

    it("should render toggle button", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByLabelText("Collapse filters")).toBeInTheDocument();
    });

    it("should show Expand label when collapsed", () => {
      render(<InventoryFilters {...defaultProps} isExpanded={false} />);

      expect(screen.getByLabelText("Expand filters")).toBeInTheDocument();
    });
  });

  describe("Filter Content Visibility", () => {
    it("should show filter inputs when expanded", () => {
      render(<InventoryFilters {...defaultProps} isExpanded={true} />);

      expect(screen.getByText("Warehouse")).toBeInTheDocument();
      expect(screen.getByText("Category")).toBeInTheDocument();
      expect(screen.getByText("Brand")).toBeInTheDocument();
      expect(screen.getByText("Stock Status")).toBeInTheDocument();
    });

    it("should hide filter inputs when collapsed", () => {
      render(<InventoryFilters {...defaultProps} isExpanded={false} />);

      expect(screen.queryByText("Warehouse")).not.toBeInTheDocument();
      expect(screen.queryByText("Category")).not.toBeInTheDocument();
    });
  });

  describe("Warehouse Filter", () => {
    it("should render warehouse dropdown", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("All Warehouses")).toBeInTheDocument();
    });

    it("should render all warehouse options", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Main Warehouse")).toBeInTheDocument();
      expect(screen.getByText("West Coast")).toBeInTheDocument();
      expect(screen.getByText("East Coast")).toBeInTheDocument();
    });

    it("should call onFilterChange when warehouse is selected", async () => {
      const user = userEvent.setup();
      const onFilterChange = vi.fn();

      render(
        <InventoryFilters {...defaultProps} onFilterChange={onFilterChange} />,
      );

      const select = screen.getByDisplayValue("All Warehouses");
      await user.selectOptions(select, "1");

      expect(onFilterChange).toHaveBeenCalledWith({
        ...defaultFilters,
        warehouseId: 1,
      });
    });

    it("should show active indicator when warehouse is selected", () => {
      const filtersWithWarehouse = { ...defaultFilters, warehouseId: 1 };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithWarehouse} />,
      );

      const label = screen.getByText("Warehouse");
      expect(label.textContent).toContain("●");
    });

    it("should highlight select when warehouse is active", () => {
      const filtersWithWarehouse = { ...defaultFilters, warehouseId: 2 };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithWarehouse} />,
      );

      const select = screen.getByDisplayValue("West Coast");
      expect(select.className).toMatch(/border-blue-500/);
      expect(select.className).toMatch(/bg-blue-50/);
    });
  });

  describe("Category Filter", () => {
    it("should render category dropdown", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("All Categories")).toBeInTheDocument();
    });

    it("should render all category options", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Electronics")).toBeInTheDocument();
      expect(screen.getByText("Furniture")).toBeInTheDocument();
      expect(screen.getByText("Clothing")).toBeInTheDocument();
    });

    it("should call onFilterChange when category is selected", async () => {
      const user = userEvent.setup();
      const onFilterChange = vi.fn();

      render(
        <InventoryFilters {...defaultProps} onFilterChange={onFilterChange} />,
      );

      const select = screen.getByDisplayValue("All Categories");
      await user.selectOptions(select, "Electronics");

      expect(onFilterChange).toHaveBeenCalledWith({
        ...defaultFilters,
        category: "Electronics",
      });
    });

    it("should show active indicator when category is selected", () => {
      const filtersWithCategory = {
        ...defaultFilters,
        category: "Electronics",
      };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithCategory} />,
      );

      const label = screen.getByText("Category");
      expect(label.textContent).toContain("●");
    });
  });

  describe("Brand Filter", () => {
    it("should render brand dropdown", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("All Brands")).toBeInTheDocument();
    });

    it("should render all brand options", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("BrandA")).toBeInTheDocument();
      expect(screen.getByText("BrandB")).toBeInTheDocument();
      expect(screen.getByText("BrandC")).toBeInTheDocument();
    });

    it("should call onFilterChange when brand is selected", async () => {
      const user = userEvent.setup();
      const onFilterChange = vi.fn();

      render(
        <InventoryFilters {...defaultProps} onFilterChange={onFilterChange} />,
      );

      const select = screen.getByDisplayValue("All Brands");
      await user.selectOptions(select, "BrandA");

      expect(onFilterChange).toHaveBeenCalledWith({
        ...defaultFilters,
        brand: "BrandA",
      });
    });

    it("should show active indicator when brand is selected", () => {
      const filtersWithBrand = { ...defaultFilters, brand: "BrandB" };

      render(<InventoryFilters {...defaultProps} filters={filtersWithBrand} />);

      const label = screen.getByText("Brand");
      expect(label.textContent).toContain("●");
    });
  });

  describe("Stock Status Filter", () => {
    it("should render stock status dropdown", () => {
      render(<InventoryFilters {...defaultProps} />);

      const selects = screen.getAllByText("All Items");
      expect(selects.length).toBeGreaterThan(0);
    });

    it("should render stock status options", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("In Stock")).toBeInTheDocument();
      expect(screen.getByText("Low Stock")).toBeInTheDocument();
      expect(screen.getByText("Out of Stock")).toBeInTheDocument();
    });

    it("should call onFilterChange when stock status is selected", async () => {
      const user = userEvent.setup();
      const onFilterChange = vi.fn();

      render(
        <InventoryFilters {...defaultProps} onFilterChange={onFilterChange} />,
      );

      const stockSelect = screen.getAllByRole("combobox")[3]; // 4th select
      await user.selectOptions(stockSelect, "low-stock");

      expect(onFilterChange).toHaveBeenCalledWith({
        ...defaultFilters,
        stockStatus: "low-stock",
      });
    });

    it("should show active indicator when stock status is selected", () => {
      const filtersWithStock = { ...defaultFilters, stockStatus: "in-stock" };

      render(<InventoryFilters {...defaultProps} filters={filtersWithStock} />);

      const label = screen.getByText("Stock Status");
      expect(label.textContent).toContain("●");
    });
  });

  describe("Quantity Range Filters", () => {
    it("should render min quantity input", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Min Quantity")).toBeInTheDocument();
      const inputs = screen.getAllByPlaceholderText("Min");
      expect(inputs.length).toBeGreaterThan(0);
    });

    it("should render max quantity input", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Max Quantity")).toBeInTheDocument();
      const inputs = screen.getAllByPlaceholderText("Max");
      expect(inputs.length).toBeGreaterThan(0);
    });

    it("should call onFilterChange when min quantity is entered", async () => {
      const user = userEvent.setup();
      let currentFilters = { ...defaultFilters };
      const onFilterChange = vi.fn((newFilters) => {
        currentFilters = newFilters;
      });

      const { rerender } = render(
        <InventoryFilters
          {...defaultProps}
          filters={currentFilters}
          onFilterChange={onFilterChange}
        />,
      );

      const inputs = screen.getAllByPlaceholderText("Min");
      const quantityInput = inputs[0] as HTMLInputElement; // First "Min" is quantity

      // Type "1"
      await user.type(quantityInput, "1");
      rerender(
        <InventoryFilters
          {...defaultProps}
          filters={currentFilters}
          onFilterChange={onFilterChange}
        />,
      );

      // Type "0"
      await user.type(quantityInput, "0");
      rerender(
        <InventoryFilters
          {...defaultProps}
          filters={currentFilters}
          onFilterChange={onFilterChange}
        />,
      );

      // Check that onFilterChange was called
      expect(onFilterChange).toHaveBeenCalled();
      const calls = onFilterChange.mock.calls;
      const quantityValues = calls.map((c) => c[0].minQuantity);

      // Should eventually have "10"
      expect(quantityValues).toContain("10");
    });

    it("should show active indicator when min quantity is set", () => {
      const filtersWithMin = { ...defaultFilters, minQuantity: "5" };

      render(<InventoryFilters {...defaultProps} filters={filtersWithMin} />);

      const label = screen.getByText("Min Quantity");
      expect(label.textContent).toContain("●");
    });

    it("should highlight input when quantity filter is active", () => {
      const filtersWithQuantity = { ...defaultFilters, maxQuantity: "100" };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithQuantity} />,
      );

      const inputs = screen.getAllByPlaceholderText("Max");
      const quantityInput = inputs[0]; // First "Max" is quantity
      expect(quantityInput.className).toMatch(/border-blue-500/);
    });
  });

  describe("Price Range Filters", () => {
    it("should render min price input", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Min Price ($)")).toBeInTheDocument();
    });

    it("should render max price input", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Max Price ($)")).toBeInTheDocument();
    });

    it("should call onFilterChange when price is entered", async () => {
      const user = userEvent.setup();
      let currentFilters = { ...defaultFilters };
      const onFilterChange = vi.fn((newFilters) => {
        currentFilters = newFilters;
      });

      const { rerender } = render(
        <InventoryFilters
          {...defaultProps}
          filters={currentFilters}
          onFilterChange={onFilterChange}
        />,
      );

      const inputs = screen.getAllByPlaceholderText("Min");
      const priceInput = inputs[1] as HTMLInputElement; // Second "Min" placeholder (price)

      // Type each character and rerender
      for (const char of "50.99") {
        await user.type(priceInput, char);
        rerender(
          <InventoryFilters
            {...defaultProps}
            filters={currentFilters}
            onFilterChange={onFilterChange}
          />,
        );
      }

      // Check that onFilterChange was called
      expect(onFilterChange).toHaveBeenCalled();

      // Check final value
      const calls = onFilterChange.mock.calls;
      const priceValues = calls.map((c) => c[0].minPrice);
      expect(priceValues).toContain("50.99");
    });

    it("should show active indicator when price is set", () => {
      const filtersWithPrice = { ...defaultFilters, minPrice: "10.00" };

      render(<InventoryFilters {...defaultProps} filters={filtersWithPrice} />);

      const label = screen.getByText("Min Price ($)");
      expect(label.textContent).toContain("●");
    });
  });

  describe("Expiration Status Filter", () => {
    it("should render expiration status dropdown", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Expiration Status")).toBeInTheDocument();
    });

    it("should render expiration options", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Expired")).toBeInTheDocument();
      expect(screen.getByText("Expiring Soon (30 days)")).toBeInTheDocument();
      expect(screen.getByText("Valid / No Expiration")).toBeInTheDocument();
    });

    it("should call onFilterChange when expiration status is selected", async () => {
      const user = userEvent.setup();
      const onFilterChange = vi.fn();

      render(
        <InventoryFilters {...defaultProps} onFilterChange={onFilterChange} />,
      );

      const expirationSelect = screen.getAllByRole("combobox")[4]; // 5th select
      await user.selectOptions(expirationSelect, "expired");

      expect(onFilterChange).toHaveBeenCalledWith({
        ...defaultFilters,
        expirationStatus: "expired",
      });
    });

    it("should show active indicator when expiration status is selected", () => {
      const filtersWithExpiration = {
        ...defaultFilters,
        expirationStatus: "expiring-soon",
      };

      render(
        <InventoryFilters {...defaultProps} filters={filtersWithExpiration} />,
      );

      const label = screen.getByText("Expiration Status");
      expect(label.textContent).toContain("●");
    });
  });

  describe("Clear All Button", () => {
    it("should call onReset when clicked", async () => {
      const user = userEvent.setup();
      const onReset = vi.fn();
      const filtersWithActive = { ...defaultFilters, category: "Electronics" };

      render(
        <InventoryFilters
          {...defaultProps}
          filters={filtersWithActive}
          onReset={onReset}
        />,
      );

      await user.click(screen.getByText("Clear all"));

      expect(onReset).toHaveBeenCalled();
    });
  });

  describe("Toggle Button", () => {
    it("should call onToggle when clicked", async () => {
      const user = userEvent.setup();
      const onToggle = vi.fn();

      render(<InventoryFilters {...defaultProps} onToggle={onToggle} />);

      await user.click(screen.getByLabelText("Collapse filters"));

      expect(onToggle).toHaveBeenCalled();
    });

    it("should rotate arrow icon when expanded", () => {
      const { container } = render(
        <InventoryFilters {...defaultProps} isExpanded={true} />,
      );

      const svg = container.querySelector("svg.rotate-180");
      expect(svg).toBeInTheDocument();
    });

    it("should not rotate arrow icon when collapsed", () => {
      const { container } = render(
        <InventoryFilters {...defaultProps} isExpanded={false} />,
      );

      const svg = container.querySelector("svg.rotate-180");
      expect(svg).not.toBeInTheDocument();
    });
  });

  describe("Active Filters Count", () => {
    it("should count warehouse filter correctly", () => {
      const filters = { ...defaultFilters, warehouseId: 1 };
      render(<InventoryFilters {...defaultProps} filters={filters} />);

      expect(screen.getByText("1 active")).toBeInTheDocument();
    });

    it("should count multiple filters correctly", () => {
      const filters = {
        ...defaultFilters,
        warehouseId: 1,
        category: "Electronics",
        brand: "BrandA",
        stockStatus: "low-stock" as const,
        minQuantity: "10",
      };
      render(<InventoryFilters {...defaultProps} filters={filters} />);

      expect(screen.getByText("5 active")).toBeInTheDocument();
    });

    it("should not count default values as active", () => {
      render(<InventoryFilters {...defaultProps} filters={defaultFilters} />);

      expect(screen.queryByText(/active/)).not.toBeInTheDocument();
    });

    it("should not count empty strings as active", () => {
      const filters = {
        ...defaultFilters,
        category: "",
        brand: "",
        minQuantity: "",
      };
      render(<InventoryFilters {...defaultProps} filters={filters} />);

      expect(screen.queryByText(/active/)).not.toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle empty warehouse list", () => {
      render(<InventoryFilters {...defaultProps} warehouses={[]} />);

      expect(screen.getByText("All Warehouses")).toBeInTheDocument();
    });

    it("should handle empty category list", () => {
      render(<InventoryFilters {...defaultProps} categories={[]} />);

      expect(screen.getByText("All Categories")).toBeInTheDocument();
    });

    it("should handle empty brand list", () => {
      render(<InventoryFilters {...defaultProps} brands={[]} />);

      expect(screen.getByText("All Brands")).toBeInTheDocument();
    });

    it("should handle all filters active at once", () => {
      const allActive = {
        warehouseId: 1,
        category: "Electronics",
        brand: "BrandA",
        stockStatus: "low-stock" as const,
        expirationStatus: "expired" as const,
        minQuantity: "10",
        maxQuantity: "100",
        minPrice: "5.00",
        maxPrice: "50.00",
      };

      render(<InventoryFilters {...defaultProps} filters={allActive} />);

      expect(screen.getByText("9 active")).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("should have proper labels for inputs", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByText("Warehouse")).toBeInTheDocument();
      expect(screen.getByText("Category")).toBeInTheDocument();
      expect(screen.getByText("Brand")).toBeInTheDocument();
      expect(screen.getByText("Stock Status")).toBeInTheDocument();
    });

    it("should have aria-label for toggle button", () => {
      render(<InventoryFilters {...defaultProps} />);

      expect(screen.getByLabelText(/filters/i)).toBeInTheDocument();
    });
  });
});
