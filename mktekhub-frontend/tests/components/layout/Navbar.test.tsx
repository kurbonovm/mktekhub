import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../utils/testUtils';
import { Navbar } from '@/components/layout/Navbar';
import { mockUsers } from '../../utils/mockData';

// Mock the useAuth hook
const mockUseAuth = vi.fn();
const mockLogout = vi.fn();
const mockNavigate = vi.fn();

vi.mock('@/contexts/AuthContext', async () => {
  const actual = await vi.importActual('@/contexts/AuthContext');
  return {
    ...actual,
    useAuth: () => mockUseAuth(),
  };
});

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Navbar', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['EMPLOYEE'],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: mockLogout,
      hasRole: vi.fn(() => false),
    });
  });

  describe('Rendering', () => {
    it('should render navigation element', () => {
      renderWithProviders(<Navbar />);

      const nav = screen.getByRole('navigation', { name: 'Main navigation' });
      expect(nav).toBeInTheDocument();
    });

    it('should render logo link to dashboard', () => {
      renderWithProviders(<Navbar />);

      const logo = screen.getByRole('link', { name: '' });
      expect(logo).toHaveAttribute('href', '/dashboard');
    });

    it('should display user name', () => {
      renderWithProviders(<Navbar />);

      // Desktop view only (mobile menu is closed by default)
      expect(screen.getByText(/John Doe/)).toBeInTheDocument();
    });

    it('should display user roles', () => {
      renderWithProviders(<Navbar />);

      // Desktop view only (mobile menu is closed by default)
      expect(screen.getByText(/EMPLOYEE/)).toBeInTheDocument();
    });

    it('should display multiple roles', () => {
      mockUseAuth.mockReturnValue({
        user: { ...mockUser, roles: ['ADMIN', 'MANAGER'] },
        logout: mockLogout,
        hasRole: vi.fn(() => true),
      });

      renderWithProviders(<Navbar />);

      // Desktop view only (mobile menu is closed by default)
      expect(screen.getByText(/ADMIN, MANAGER/)).toBeInTheDocument();
    });
  });

  describe('Desktop Navigation', () => {
    it('should render all common navigation links', () => {
      renderWithProviders(<Navbar />);

      expect(screen.getAllByRole('link', { name: 'Dashboard' })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('link', { name: 'Warehouses' })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('link', { name: 'Inventory' })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('link', { name: 'Activity' })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('link', { name: 'Reports' })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('link', { name: 'Custom Reports' })[0]).toBeInTheDocument();
    });

    it('should not show admin links for regular users', () => {
      renderWithProviders(<Navbar />);

      // Desktop menu (hidden by CSS but still in DOM)
      const stockTransferLinks = screen.queryAllByRole('link', { name: 'Stock Transfer' });
      const bulkTransferLinks = screen.queryAllByRole('link', { name: 'Bulk Transfer' });

      expect(stockTransferLinks.length).toBe(0);
      expect(bulkTransferLinks.length).toBe(0);
    });

    it('should show admin links for admin users', () => {
      const hasRole = vi.fn((role) => role === 'ADMIN');
      mockUseAuth.mockReturnValue({
        user: mockUsers.admin,
        logout: mockLogout,
        hasRole,
      });

      renderWithProviders(<Navbar />);

      expect(screen.getAllByRole('link', { name: 'Stock Transfer' }).length).toBeGreaterThan(0);
      expect(screen.getAllByRole('link', { name: 'Bulk Transfer' }).length).toBeGreaterThan(0);
    });

    it('should show admin links for manager users', () => {
      const hasRole = vi.fn((role) => role === 'MANAGER');
      mockUseAuth.mockReturnValue({
        user: mockUsers.manager,
        logout: mockLogout,
        hasRole,
      });

      renderWithProviders(<Navbar />);

      expect(screen.getAllByRole('link', { name: 'Stock Transfer' }).length).toBeGreaterThan(0);
      expect(screen.getAllByRole('link', { name: 'Bulk Transfer' }).length).toBeGreaterThan(0);
    });
  });

  describe('Active Link Styling', () => {
    it('should highlight active dashboard link', () => {
      renderWithProviders(<Navbar />, { initialRoute: '/dashboard' });

      const dashboardLinks = screen.getAllByRole('link', { name: 'Dashboard' });
      expect(dashboardLinks[0].className).toContain('bg-blue-700');
    });

    it('should highlight active inventory link', () => {
      renderWithProviders(<Navbar />, { initialRoute: '/inventory' });

      const inventoryLinks = screen.getAllByRole('link', { name: 'Inventory' });
      expect(inventoryLinks[0].className).toContain('bg-blue-700');
    });

    it('should not highlight inactive links', () => {
      renderWithProviders(<Navbar />, { initialRoute: '/dashboard' });

      const inventoryLinks = screen.getAllByRole('link', { name: 'Inventory' });
      // Inactive link should have "text-white hover:bg-blue-700" not just "bg-blue-700"
      expect(inventoryLinks[0].className).toContain('text-white');
      expect(inventoryLinks[0].className).not.toContain('bg-blue-700 text-white');
    });
  });

  describe('Logout Functionality', () => {
    it('should render logout button', () => {
      renderWithProviders(<Navbar />);

      const logoutButtons = screen.getAllByRole('button', { name: /Logout/ });
      expect(logoutButtons.length).toBeGreaterThan(0);
    });

    it('should call logout when logout button is clicked', async () => {
      const user = userEvent.setup();
      renderWithProviders(<Navbar />);

      const logoutButton = screen.getAllByRole('button', { name: /Logout/ })[0];
      await user.click(logoutButton);

      expect(mockLogout).toHaveBeenCalledTimes(1);
    });

    it('should navigate to login after logout', async () => {
      const user = userEvent.setup();
      renderWithProviders(<Navbar />);

      const logoutButton = screen.getAllByRole('button', { name: /Logout/ })[0];
      await user.click(logoutButton);

      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  describe('Mobile Menu', () => {
    it('should render mobile menu button', () => {
      renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      expect(menuButton).toBeInTheDocument();
    });

    it('should not show mobile menu by default', () => {
      const { container } = renderWithProviders(<Navbar />);

      const backdrop = container.querySelector('.bg-black\\/30');
      expect(backdrop).not.toBeInTheDocument();
    });

    it('should open mobile menu when button clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      const backdrop = container.querySelector('.bg-black\\/30');
      expect(backdrop).toBeInTheDocument();
    });

    it('should close mobile menu when button clicked again', async () => {
      const user = userEvent.setup();
      const { container } = renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });

      // Open menu
      await user.click(menuButton);
      expect(container.querySelector('.bg-black\\/30')).toBeInTheDocument();

      // Close menu
      await user.click(menuButton);
      expect(container.querySelector('.bg-black\\/30')).not.toBeInTheDocument();
    });

    it('should close mobile menu when backdrop is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      const backdrop = container.querySelector('.bg-black\\/30');
      expect(backdrop).toBeInTheDocument();

      await user.click(backdrop!);
      expect(container.querySelector('.bg-black\\/30')).not.toBeInTheDocument();
    });

    it('should show correct icon when menu is closed', () => {
      renderWithProviders(<Navbar />);

      const srText = screen.getByText('Open main menu');
      expect(srText).toBeInTheDocument();
    });

    it('should show correct icon when menu is open', async () => {
      const user = userEvent.setup();
      renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      const srText = screen.getByText('Close main menu');
      expect(srText).toBeInTheDocument();
    });

    it('should have proper aria-expanded attribute', async () => {
      const user = userEvent.setup();
      renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });

      expect(menuButton).toHaveAttribute('aria-expanded', 'false');

      await user.click(menuButton);
      expect(menuButton).toHaveAttribute('aria-expanded', 'true');
    });
  });

  describe('Mobile Navigation Links', () => {
    it('should render mobile navigation links when menu is open', async () => {
      const user = userEvent.setup();
      renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      // Check mobile-specific text
      expect(screen.getByText('Activity History')).toBeInTheDocument();
    });

    it('should close mobile menu when link is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      const dashboardLinks = screen.getAllByRole('link', { name: 'Dashboard' });
      const mobileLink = dashboardLinks[dashboardLinks.length - 1]; // Last one is mobile

      await user.click(mobileLink);

      expect(container.querySelector('.bg-black\\/30')).not.toBeInTheDocument();
    });

    it('should close mobile menu after logout', async () => {
      const user = userEvent.setup();
      const { container } = renderWithProviders(<Navbar />);

      const menuButton = screen.getByRole('button', { name: 'Toggle navigation menu' });
      await user.click(menuButton);

      const logoutButtons = screen.getAllByRole('button', { name: /Logout/ });
      const mobileLogoutButton = logoutButtons[logoutButtons.length - 1];

      await user.click(mobileLogoutButton);

      expect(container.querySelector('.bg-black\\/30')).not.toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper navigation aria-label', () => {
      renderWithProviders(<Navbar />);

      const nav = screen.getByRole('navigation', { name: 'Main navigation' });
      expect(nav).toBeInTheDocument();
    });

    it('should have proper logout button aria-label', () => {
      renderWithProviders(<Navbar />);

      const logoutButton = screen.getByRole('button', { name: 'Logout from application' });
      expect(logoutButton).toBeInTheDocument();
    });

    it('should have sr-only text for mobile menu', () => {
      renderWithProviders(<Navbar />);

      const srText = screen.getByText('Open main menu');
      expect(srText.className).toContain('sr-only');
    });

    it('should mark decorative icons as aria-hidden', () => {
      const { container } = renderWithProviders(<Navbar />);

      const hiddenIcons = container.querySelectorAll('[aria-hidden="true"]');
      expect(hiddenIcons.length).toBeGreaterThan(0);
    });
  });

  describe('Responsive Behavior', () => {
    it('should have hidden desktop menu class', () => {
      const { container } = renderWithProviders(<Navbar />);

      const desktopMenu = container.querySelector('.hidden.md\\:flex');
      expect(desktopMenu).toBeInTheDocument();
    });

    it('should have visible mobile menu button class', () => {
      const { container } = renderWithProviders(<Navbar />);

      const mobileButton = container.querySelector('.flex.md\\:hidden');
      expect(mobileButton).toBeInTheDocument();
    });

    it('should have responsive hidden columns in desktop view', () => {
      const { container } = renderWithProviders(<Navbar />);

      const hiddenDesktop = container.querySelector('.hidden.md\\:flex');
      expect(hiddenDesktop).toBeInTheDocument();
    });
  });

  describe('Styling', () => {
    it('should have sticky positioning', () => {
      const { container } = renderWithProviders(<Navbar />);

      const nav = container.querySelector('.sticky');
      expect(nav).toBeInTheDocument();
    });

    it('should have blue background', () => {
      const { container } = renderWithProviders(<Navbar />);

      const nav = container.querySelector('.bg-blue-600');
      expect(nav).toBeInTheDocument();
    });

    it('should have shadow', () => {
      const { container } = renderWithProviders(<Navbar />);

      const nav = container.querySelector('.shadow-lg');
      expect(nav).toBeInTheDocument();
    });

    it('should have high z-index', () => {
      const { container } = renderWithProviders(<Navbar />);

      const nav = container.querySelector('.z-50');
      expect(nav).toBeInTheDocument();
    });
  });
});
