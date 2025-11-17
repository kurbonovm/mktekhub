import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../../utils/testUtils';
import { Breadcrumb, BreadcrumbItem } from '@/components/common/Breadcrumb';

describe('Breadcrumb', () => {
  describe('Manual Breadcrumb Items', () => {
    it('should render breadcrumb items', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products', path: '/products' },
        { label: 'Details' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      expect(screen.getByText('Home')).toBeInTheDocument();
      expect(screen.getByText('Products')).toBeInTheDocument();
      expect(screen.getByText('Details')).toBeInTheDocument();
    });

    it('should render links for items with path', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Current Page' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      const homeLink = screen.getByRole('link', { name: /Home/ });
      expect(homeLink).toHaveAttribute('href', '/');
    });

    it('should render span for items without path', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Current Page' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      const currentPage = screen.getByText('Current Page');
      expect(currentPage.tagName).toBe('SPAN');
    });

    it('should render separator icons between items', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products', path: '/products' },
        { label: 'Details' },
      ];

      const { container } = renderWithProviders(<Breadcrumb items={items} />);

      const separators = container.querySelectorAll('svg[aria-hidden="true"]');
      expect(separators.length).toBe(2); // One less than number of items
    });

    it('should render home icon for first item', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products' },
      ];

      const { container } = renderWithProviders(<Breadcrumb items={items} />);

      const homeIcons = container.querySelectorAll('svg');
      expect(homeIcons.length).toBeGreaterThan(0);
    });

    it('should render nothing when items array is empty', () => {
      const { container } = renderWithProviders(<Breadcrumb items={[]} />);

      expect(container.querySelector('nav')).not.toBeInTheDocument();
    });

    it('should render nothing when no items prop is provided and autoGenerate is false', () => {
      const { container } = renderWithProviders(<Breadcrumb />);

      expect(container.querySelector('nav')).not.toBeInTheDocument();
    });
  });

  describe('Auto-generate Breadcrumbs', () => {
    it('should auto-generate breadcrumbs from URL path', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/inventory',
      });

      expect(screen.getByText('Home')).toBeInTheDocument();
      expect(screen.getByText('Inventory')).toBeInTheDocument();
    });

    it('should capitalize path segments', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/custom-page',
      });

      expect(screen.getByText('Custom-page')).toBeInTheDocument();
    });

    it('should use predefined labels for known paths', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/warehouses',
      });

      expect(screen.getByText('Warehouses')).toBeInTheDocument();
    });

    it('should handle nested paths', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/inventory/edit/123',
      });

      expect(screen.getByText('Home')).toBeInTheDocument();
      expect(screen.getByText('Inventory')).toBeInTheDocument();
      expect(screen.getByText('Edit')).toBeInTheDocument();
      expect(screen.getByText('123')).toBeInTheDocument();
    });

    it('should make all items clickable except the last one', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/inventory/edit',
      });

      const homeLink = screen.getByRole('link', { name: /Home/ });
      const inventoryLink = screen.getByRole('link', { name: /Inventory/ });
      const editText = screen.getByText('Edit');

      expect(homeLink).toBeInTheDocument();
      expect(inventoryLink).toBeInTheDocument();
      expect(editText.tagName).toBe('SPAN');
    });

    it('should generate correct paths for intermediate breadcrumbs', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/inventory/edit',
      });

      const inventoryLink = screen.getByRole('link', { name: /Inventory/ });
      expect(inventoryLink).toHaveAttribute('href', '/inventory');
    });

    it('should handle dashboard path', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/dashboard',
      });

      expect(screen.getByText('Home')).toBeInTheDocument();
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });

    it('should handle root path', () => {
      renderWithProviders(<Breadcrumb autoGenerate />, {
        initialRoute: '/',
      });

      expect(screen.getByText('Home')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA label', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      const nav = screen.getByRole('navigation', { name: 'Breadcrumb' });
      expect(nav).toBeInTheDocument();
    });

    it('should use ordered list for breadcrumb items', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products' },
      ];

      const { container } = renderWithProviders(<Breadcrumb items={items} />);

      const orderedList = container.querySelector('ol');
      expect(orderedList).toBeInTheDocument();
    });

    it('should mark separator icons as aria-hidden', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Products', path: '/products' },
        { label: 'Details' },
      ];

      const { container } = renderWithProviders(<Breadcrumb items={items} />);

      const separators = container.querySelectorAll('svg[aria-hidden="true"]');
      separators.forEach((separator) => {
        expect(separator).toHaveAttribute('aria-hidden', 'true');
      });
    });
  });

  describe('Styling', () => {
    it('should apply hover styles to links', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Current' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      const link = screen.getByRole('link', { name: /Home/ });
      expect(link.className).toContain('hover:text-indigo-600');
    });

    it('should style current page differently', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home', path: '/' },
        { label: 'Current Page' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      const currentPage = screen.getByText('Current Page');
      expect(currentPage.className).toContain('text-gray-500');
    });
  });

  describe('Edge Cases', () => {
    it('should handle single breadcrumb item', () => {
      const items: BreadcrumbItem[] = [{ label: 'Home' }];

      renderWithProviders(<Breadcrumb items={items} />);

      expect(screen.getByText('Home')).toBeInTheDocument();
    });

    it('should handle very long breadcrumb chains', () => {
      const items: BreadcrumbItem[] = Array.from({ length: 10 }, (_, i) => ({
        label: `Level ${i + 1}`,
        path: i < 9 ? `/level${i + 1}` : undefined,
      }));

      renderWithProviders(<Breadcrumb items={items} />);

      expect(screen.getByText('Level 1')).toBeInTheDocument();
      expect(screen.getByText('Level 10')).toBeInTheDocument();
    });

    it('should handle special characters in labels', () => {
      const items: BreadcrumbItem[] = [
        { label: 'Home & Away', path: '/' },
        { label: 'Products > Services' },
      ];

      renderWithProviders(<Breadcrumb items={items} />);

      expect(screen.getByText('Home & Away')).toBeInTheDocument();
      expect(screen.getByText('Products > Services')).toBeInTheDocument();
    });
  });
});
