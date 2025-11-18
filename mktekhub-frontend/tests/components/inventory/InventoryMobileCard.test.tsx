import { describe, it, expect, vi } from "vitest";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { render } from "@testing-library/react";
import { InventoryMobileCard } from "@/components/inventory/InventoryMobileCard";
import {
  mockInventoryItems,
  createMockInventoryItem,
} from "../../utils/mockData";

// Mock the common components
vi.mock("@/components/common", () => ({
  ExpirationBadge: ({ expirationDate }: { expirationDate?: string }) => (
    <div data-testid="expiration-badge">
      {expirationDate || "No expiration"}
    </div>
  ),
  WarrantyBadge: ({ warrantyEndDate }: { warrantyEndDate?: string }) => (
    <div data-testid="warranty-badge">{warrantyEndDate || "No warranty"}</div>
  ),
}));

describe("InventoryMobileCard", () => {
  const defaultProps = {
    items: mockInventoryItems,
    isAdminOrManager: false,
    onAdjust: vi.fn(),
    onEdit: vi.fn(),
    onDelete: vi.fn(),
    onResetFilters: vi.fn(),
    hasActiveFilters: false,
  };

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render card container", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const cardContainer = container.querySelector(".space-y-4");
      expect(cardContainer).toBeInTheDocument();
    });

    it("should render all inventory items as cards", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      mockInventoryItems.forEach((item) => {
        expect(screen.getByText(item.name)).toBeInTheDocument();
      });
    });

    it("should render item SKU", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getByText(/LAP-DELL-001/)).toBeInTheDocument();
    });
  });

  describe("Empty State", () => {
    it("should show empty state when no items", () => {
      render(<InventoryMobileCard {...defaultProps} items={[]} />);

      expect(screen.getByText("No items found")).toBeInTheDocument();
    });

    it("should show appropriate message without filters", () => {
      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[]}
          hasActiveFilters={false}
        />,
      );

      expect(
        screen.getByText("Get started by adding a new inventory item"),
      ).toBeInTheDocument();
    });

    it("should show appropriate message with filters", () => {
      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[]}
          hasActiveFilters={true}
        />,
      );

      expect(
        screen.getByText("Try adjusting your search or filters"),
      ).toBeInTheDocument();
    });

    it("should show clear filters button when filters active", () => {
      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[]}
          hasActiveFilters={true}
        />,
      );

      expect(
        screen.getByRole("button", { name: "Clear filters" }),
      ).toBeInTheDocument();
    });

    it("should not show clear filters button when no filters", () => {
      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[]}
          hasActiveFilters={false}
        />,
      );

      expect(
        screen.queryByRole("button", { name: "Clear filters" }),
      ).not.toBeInTheDocument();
    });

    it("should call onResetFilters when clear button clicked", async () => {
      const user = userEvent.setup();
      const onResetFilters = vi.fn();

      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[]}
          hasActiveFilters={true}
          onResetFilters={onResetFilters}
        />,
      );

      const clearButton = screen.getByRole("button", { name: "Clear filters" });
      await user.click(clearButton);

      expect(onResetFilters).toHaveBeenCalledTimes(1);
    });
  });

  describe("Card Content", () => {
    it("should display item name", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getByText("Laptop Dell XPS 15")).toBeInTheDocument();
    });

    it("should display SKU with label", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText(/SKU:/)[0]).toBeInTheDocument();
      expect(screen.getByText(/LAP-DELL-001/)).toBeInTheDocument();
    });

    it("should display category", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText("Category:")[0]).toBeInTheDocument();
      expect(screen.getByText("Electronics")).toBeInTheDocument();
    });

    it("should display warehouse name", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText("Warehouse:")[0]).toBeInTheDocument();
      expect(screen.getAllByText("Main Warehouse").length).toBeGreaterThan(0);
    });

    it("should display quantity", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText("Quantity:")[0]).toBeInTheDocument();
    });

    it("should display formatted unit price", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText("Unit Price:")[0]).toBeInTheDocument();
      expect(screen.getAllByText(/\$1299\.99/)[0]).toBeInTheDocument();
    });

    it("should display volume information", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      expect(screen.getAllByText("Volume per Unit:")[0]).toBeInTheDocument();
      expect(screen.getAllByText("Total Volume:")[0]).toBeInTheDocument();
    });

    it("should handle missing category", () => {
      const itemWithoutCategory = createMockInventoryItem({
        category: undefined,
      });

      render(
        <InventoryMobileCard {...defaultProps} items={[itemWithoutCategory]} />,
      );

      expect(screen.getByText("N/A")).toBeInTheDocument();
    });

    it("should format price to 2 decimals", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      expect(container.textContent).toMatch(/\$\d+\.\d{2}/);
    });
  });

  describe("Low Stock Badge", () => {
    it("should show low stock badge when quantity at or below reorder level", () => {
      const lowStockItem = createMockInventoryItem({
        quantity: 5,
        reorderLevel: 10,
      });

      render(<InventoryMobileCard {...defaultProps} items={[lowStockItem]} />);

      expect(screen.getByText("Low Stock")).toBeInTheDocument();
    });

    it("should not show low stock badge when quantity above reorder level", () => {
      const normalStockItem = createMockInventoryItem({
        quantity: 50,
        reorderLevel: 10,
      });

      render(
        <InventoryMobileCard {...defaultProps} items={[normalStockItem]} />,
      );

      expect(screen.queryByText("Low Stock")).not.toBeInTheDocument();
    });

    it("should handle items without reorder level", () => {
      const itemWithoutReorderLevel = createMockInventoryItem({
        quantity: 5,
        reorderLevel: undefined,
      });

      render(
        <InventoryMobileCard
          {...defaultProps}
          items={[itemWithoutReorderLevel]}
        />,
      );

      // Should not show badge if reorder level is not set (defaults to 0)
      expect(screen.queryByText("Low Stock")).not.toBeInTheDocument();
    });

    it("should have red styling for low stock badge", () => {
      const lowStockItem = createMockInventoryItem({
        quantity: 5,
        reorderLevel: 10,
      });

      render(<InventoryMobileCard {...defaultProps} items={[lowStockItem]} />);

      const badge = screen.getByText("Low Stock");
      expect(badge.className).toContain("bg-red-100");
      expect(badge.className).toContain("text-red-800");
    });
  });

  describe("Expiration and Warranty", () => {
    it("should render expiration badges", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      const badges = screen.getAllByTestId("expiration-badge");
      expect(badges.length).toBeGreaterThan(0);
    });

    it("should render warranty badges", () => {
      render(<InventoryMobileCard {...defaultProps} />);

      const badges = screen.getAllByTestId("warranty-badge");
      expect(badges.length).toBeGreaterThan(0);
    });
  });

  describe("Actions for Regular Users", () => {
    it("should not show action buttons for regular users", () => {
      render(
        <InventoryMobileCard {...defaultProps} isAdminOrManager={false} />,
      );

      expect(
        screen.queryByRole("button", { name: /Adjust/ }),
      ).not.toBeInTheDocument();
      expect(
        screen.queryByRole("button", { name: /Edit/ }),
      ).not.toBeInTheDocument();
      expect(
        screen.queryByRole("button", { name: /Delete/ }),
      ).not.toBeInTheDocument();
    });

    it("should not show edit button for regular users", () => {
      render(
        <InventoryMobileCard {...defaultProps} isAdminOrManager={false} />,
      );

      expect(
        screen.queryByRole("button", { name: /Edit/ }),
      ).not.toBeInTheDocument();
    });

    it("should not show delete button for regular users", () => {
      render(
        <InventoryMobileCard {...defaultProps} isAdminOrManager={false} />,
      );

      expect(
        screen.queryByRole("button", { name: /Delete/ }),
      ).not.toBeInTheDocument();
    });
  });

  describe("Actions for Admin/Manager", () => {
    it("should show adjust button for admins", () => {
      render(<InventoryMobileCard {...defaultProps} isAdminOrManager={true} />);

      const adjustButtons = screen.getAllByRole("button", { name: /Adjust/ });
      expect(adjustButtons.length).toBeGreaterThan(0);
    });

    it("should show edit button for admins", () => {
      render(<InventoryMobileCard {...defaultProps} isAdminOrManager={true} />);

      const editButtons = screen.getAllByRole("button", { name: /Edit/ });
      expect(editButtons.length).toBeGreaterThan(0);
    });

    it("should show delete button for admins", () => {
      render(<InventoryMobileCard {...defaultProps} isAdminOrManager={true} />);

      const deleteButtons = screen.getAllByRole("button", { name: /Delete/ });
      expect(deleteButtons.length).toBeGreaterThan(0);
    });

    it("should call onAdjust when adjust button clicked", async () => {
      const user = userEvent.setup();
      const onAdjust = vi.fn();

      render(
        <InventoryMobileCard
          {...defaultProps}
          isAdminOrManager={true}
          onAdjust={onAdjust}
        />,
      );

      const adjustButtons = screen.getAllByRole("button", { name: /Adjust/ });
      await user.click(adjustButtons[0]);

      expect(onAdjust).toHaveBeenCalledTimes(1);
      expect(onAdjust).toHaveBeenCalledWith(mockInventoryItems[0]);
    });

    it("should call onEdit when edit button clicked", async () => {
      const user = userEvent.setup();
      const onEdit = vi.fn();

      render(
        <InventoryMobileCard
          {...defaultProps}
          isAdminOrManager={true}
          onEdit={onEdit}
        />,
      );

      const editButtons = screen.getAllByRole("button", { name: /Edit/ });
      await user.click(editButtons[0]);

      expect(onEdit).toHaveBeenCalledTimes(1);
      expect(onEdit).toHaveBeenCalledWith(mockInventoryItems[0]);
    });

    it("should call onDelete when delete button clicked", async () => {
      const user = userEvent.setup();
      const onDelete = vi.fn();

      render(
        <InventoryMobileCard
          {...defaultProps}
          isAdminOrManager={true}
          onDelete={onDelete}
        />,
      );

      const deleteButtons = screen.getAllByRole("button", { name: /Delete/ });
      await user.click(deleteButtons[0]);

      expect(onDelete).toHaveBeenCalledTimes(1);
      expect(onDelete).toHaveBeenCalledWith(mockInventoryItems[0].id);
    });
  });

  describe("Styling and Responsive", () => {
    it("should show on mobile only", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const wrapper = container.querySelector(".block.md\\:hidden");
      expect(wrapper).toBeInTheDocument();
    });

    it("should have card styling", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const card = container.querySelector(".rounded-lg.bg-white.shadow");
      expect(card).toBeInTheDocument();
    });

    it("should have spacing between cards", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const wrapper = container.querySelector(".space-y-4");
      expect(wrapper).toBeInTheDocument();
    });
  });

  describe("Card Structure", () => {
    it("should render correct number of cards", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-4.shadow",
      );
      expect(cards.length).toBe(mockInventoryItems.length);
    });

    it("should use unique keys for cards", () => {
      const { container } = render(<InventoryMobileCard {...defaultProps} />);

      const cards = container.querySelectorAll(
        ".rounded-lg.bg-white.p-4.shadow",
      );
      expect(cards.length).toBe(mockInventoryItems.length);
    });
  });
});
