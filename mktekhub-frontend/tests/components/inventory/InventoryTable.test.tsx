import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { InventoryTable } from '@/components/inventory/InventoryTable';
import { mockInventoryItems } from '../../utils/mockData';

// Mock the common components
vi.mock('@/components/common', () => ({
  ExpirationBadge: ({ expirationDate }: any) => (
    <div data-testid="expiration-badge">{expirationDate || 'No expiration'}</div>
  ),
  WarrantyBadge: ({ warrantyEndDate }: any) => (
    <div data-testid="warranty-badge">{warrantyEndDate || 'No warranty'}</div>
  ),
  Tooltip: ({ content, children }: any) => <div title={content}>{children}</div>,
}));

describe('InventoryTable', () => {
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

  describe('Rendering', () => {
    it('should render table', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const table = container.querySelector('table');
      expect(table).toBeInTheDocument();
    });

    it('should render table headers', () => {
      render(<InventoryTable {...defaultProps} />);

      expect(screen.getByText('SKU')).toBeInTheDocument();
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Category')).toBeInTheDocument();
      expect(screen.getByText('Warehouse')).toBeInTheDocument();
      expect(screen.getByText('Quantity')).toBeInTheDocument();
      expect(screen.getByText('Price')).toBeInTheDocument();
      expect(screen.getByText('Actions')).toBeInTheDocument();
    });

    it('should render inventory items', () => {
      render(<InventoryTable {...defaultProps} />);

      expect(screen.getByText('Laptop Dell XPS 15')).toBeInTheDocument();
      expect(screen.getByText('LAP-DELL-001')).toBeInTheDocument();
    });

    it('should render all items in the list', () => {
      render(<InventoryTable {...defaultProps} />);

      mockInventoryItems.forEach((item) => {
        expect(screen.getByText(item.name)).toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should show empty state when no items', () => {
      render(<InventoryTable {...defaultProps} items={[]} />);

      expect(screen.getByText('No items found')).toBeInTheDocument();
    });

    it('should show appropriate message without filters', () => {
      render(<InventoryTable {...defaultProps} items={[]} hasActiveFilters={false} />);

      expect(screen.getByText('Get started by adding a new inventory item')).toBeInTheDocument();
    });

    it('should show appropriate message with filters', () => {
      render(<InventoryTable {...defaultProps} items={[]} hasActiveFilters={true} />);

      expect(screen.getByText('Try adjusting your search or filters')).toBeInTheDocument();
    });

    it('should show clear filters button when filters active', () => {
      render(<InventoryTable {...defaultProps} items={[]} hasActiveFilters={true} />);

      expect(screen.getByRole('button', { name: 'Clear filters' })).toBeInTheDocument();
    });

    it('should not show clear filters button when no filters', () => {
      render(<InventoryTable {...defaultProps} items={[]} hasActiveFilters={false} />);

      expect(screen.queryByRole('button', { name: 'Clear filters' })).not.toBeInTheDocument();
    });

    it('should call onResetFilters when clear button clicked', async () => {
      const user = userEvent.setup();
      const onResetFilters = vi.fn();

      render(
        <InventoryTable
          {...defaultProps}
          items={[]}
          hasActiveFilters={true}
          onResetFilters={onResetFilters}
        />
      );

      const clearButton = screen.getByRole('button', { name: 'Clear filters' });
      await user.click(clearButton);

      expect(onResetFilters).toHaveBeenCalledTimes(1);
    });
  });

  describe('Item Display', () => {
    it('should display item SKU', () => {
      render(<InventoryTable {...defaultProps} />);

      expect(screen.getByText('LAP-DELL-001')).toBeInTheDocument();
    });

    it('should display item name', () => {
      render(<InventoryTable {...defaultProps} />);

      expect(screen.getByText('Laptop Dell XPS 15')).toBeInTheDocument();
    });

    it('should display warehouse name', () => {
      render(<InventoryTable {...defaultProps} />);

      expect(screen.getAllByText('Main Warehouse')[0]).toBeInTheDocument();
    });

    it('should display quantity', () => {
      render(<InventoryTable {...defaultProps} />);

      const quantities = screen.getAllByText('50');
      expect(quantities.length).toBeGreaterThan(0);
    });

    it('should format price correctly', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      expect(container.textContent).toContain('$1299.99');
    });

    it('should render expiration badges', () => {
      render(<InventoryTable {...defaultProps} />);

      const badges = screen.getAllByTestId('expiration-badge');
      expect(badges.length).toBeGreaterThan(0);
    });

    it('should render warranty badges', () => {
      render(<InventoryTable {...defaultProps} />);

      const badges = screen.getAllByTestId('warranty-badge');
      expect(badges.length).toBeGreaterThan(0);
    });
  });

  describe('Actions for Regular Users', () => {
    it('should show "View Only" text for regular users', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={false} />);

      const viewOnlyText = screen.getAllByText('View Only');
      expect(viewOnlyText.length).toBeGreaterThan(0);
    });

    it('should not show edit button for regular users', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={false} />);

      expect(screen.queryByRole('button', { name: /Edit/ })).not.toBeInTheDocument();
    });

    it('should not show delete button for regular users', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={false} />);

      expect(screen.queryByRole('button', { name: /Delete/ })).not.toBeInTheDocument();
    });

    it('should not show adjust button for regular users', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={false} />);

      expect(screen.queryByRole('button', { name: /Adjust/ })).not.toBeInTheDocument();
    });
  });

  describe('Actions for Admin/Manager', () => {
    it('should show adjust button for admins', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={true} />);

      const adjustButtons = screen.getAllByRole('button', { name: /Adjust/ });
      expect(adjustButtons.length).toBeGreaterThan(0);
    });

    it('should show edit button for admins', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={true} />);

      const editButtons = screen.getAllByRole('button', { name: /Edit/ });
      expect(editButtons.length).toBeGreaterThan(0);
    });

    it('should show delete button for admins', () => {
      render(<InventoryTable {...defaultProps} isAdminOrManager={true} />);

      const deleteButtons = screen.getAllByRole('button', { name: /Delete/ });
      expect(deleteButtons.length).toBeGreaterThan(0);
    });

    it('should call onAdjust when adjust button clicked', async () => {
      const user = userEvent.setup();
      const onAdjust = vi.fn();

      render(
        <InventoryTable
          {...defaultProps}
          isAdminOrManager={true}
          onAdjust={onAdjust}
        />
      );

      const adjustButtons = screen.getAllByRole('button', { name: /Adjust/ });
      await user.click(adjustButtons[0]);

      expect(onAdjust).toHaveBeenCalledTimes(1);
      expect(onAdjust).toHaveBeenCalledWith(mockInventoryItems[0]);
    });

    it('should call onEdit when edit button clicked', async () => {
      const user = userEvent.setup();
      const onEdit = vi.fn();

      render(
        <InventoryTable
          {...defaultProps}
          isAdminOrManager={true}
          onEdit={onEdit}
        />
      );

      const editButtons = screen.getAllByRole('button', { name: /Edit/ });
      await user.click(editButtons[0]);

      expect(onEdit).toHaveBeenCalledTimes(1);
      expect(onEdit).toHaveBeenCalledWith(mockInventoryItems[0]);
    });

    it('should call onDelete when delete button clicked', async () => {
      const user = userEvent.setup();
      const onDelete = vi.fn();

      render(
        <InventoryTable
          {...defaultProps}
          isAdminOrManager={true}
          onDelete={onDelete}
        />
      );

      const deleteButtons = screen.getAllByRole('button', { name: /Delete/ });
      await user.click(deleteButtons[0]);

      expect(onDelete).toHaveBeenCalledTimes(1);
      expect(onDelete).toHaveBeenCalledWith(mockInventoryItems[0].id);
    });
  });

  describe('Styling and Responsive', () => {
    it('should hide on mobile', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const wrapper = container.querySelector('.hidden.md\\:block');
      expect(wrapper).toBeInTheDocument();
    });

    it('should have scrollable container', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const scrollable = container.querySelector('.overflow-x-auto');
      expect(scrollable).toBeInTheDocument();
    });

    it('should have max height for table body', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const scrollableY = container.querySelector('.overflow-y-auto');
      expect(scrollableY).toBeInTheDocument();
    });

    it('should have sticky header', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const stickyHeader = container.querySelector('thead.sticky');
      expect(stickyHeader).toBeInTheDocument();
    });
  });

  describe('Table Structure', () => {
    it('should have thead element', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const thead = container.querySelector('thead');
      expect(thead).toBeInTheDocument();
    });

    it('should have tbody element', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const tbody = container.querySelector('tbody');
      expect(tbody).toBeInTheDocument();
    });

    it('should render correct number of rows', () => {
      const { container } = render(<InventoryTable {...defaultProps} />);

      const rows = container.querySelectorAll('tbody tr');
      expect(rows.length).toBe(mockInventoryItems.length);
    });
  });
});
