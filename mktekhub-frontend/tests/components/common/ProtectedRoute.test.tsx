import { describe, it, expect, vi, afterEach } from "vitest";
import { screen } from "@testing-library/react";
import { renderWithProviders } from "../../utils/testUtils";
import { ProtectedRoute } from "@/components/common/ProtectedRoute";

// Mock the useAuth hook
const mockUseAuth = vi.fn();

vi.mock("@/contexts/AuthContext", async () => {
  const actual = await vi.importActual("@/contexts/AuthContext");
  return {
    ...actual,
    useAuth: () => mockUseAuth(),
  };
});

describe("ProtectedRoute", () => {
  const ProtectedContent = () => <div>Protected Content</div>;

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe("Loading State", () => {
    it("should show loading indicator when isLoading is true", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        hasRole: vi.fn(),
      });

      renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();
      expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    });

    it("should display loading in centered container", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        hasRole: vi.fn(),
      });

      const { container } = renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      const loadingContainer = container.querySelector(
        ".flex.h-screen.items-center.justify-center",
      );
      expect(loadingContainer).toBeInTheDocument();
    });
  });

  describe("Authentication", () => {
    it("should redirect to login when not authenticated", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        hasRole: vi.fn(),
      });

      renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      // When redirected, the protected content should not be visible
      expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    });

    it("should render children when authenticated", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => true),
      });

      renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("should not redirect when authenticated", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => true),
      });

      renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });
  });

  describe("Role-Based Access", () => {
    it("should render children when user has required role", () => {
      const hasRole = vi.fn(() => true);

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole,
      });

      renderWithProviders(
        <ProtectedRoute requiredRole="ADMIN">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(hasRole).toHaveBeenCalledWith("ADMIN");
      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("should show access denied when user lacks required role", () => {
      const hasRole = vi.fn(() => false);

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole,
      });

      renderWithProviders(
        <ProtectedRoute requiredRole="ADMIN">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(hasRole).toHaveBeenCalledWith("ADMIN");
      expect(screen.getByText("Access Denied")).toBeInTheDocument();
      expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    });

    it("should render children when no role is required", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
      });

      renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("should check for MANAGER role", () => {
      const hasRole = vi.fn(() => true);

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole,
      });

      renderWithProviders(
        <ProtectedRoute requiredRole="MANAGER">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(hasRole).toHaveBeenCalledWith("MANAGER");
      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("should check for EMPLOYEE role", () => {
      const hasRole = vi.fn(() => true);

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole,
      });

      renderWithProviders(
        <ProtectedRoute requiredRole="EMPLOYEE">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(hasRole).toHaveBeenCalledWith("EMPLOYEE");
      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });
  });

  describe("Access Denied Styling", () => {
    it("should display access denied in red text", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      });

      renderWithProviders(
        <ProtectedRoute requiredRole="ADMIN">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      const accessDenied = screen.getByText("Access Denied");
      expect(accessDenied.className).toContain("text-red-600");
    });

    it("should center access denied message", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      });

      const { container } = renderWithProviders(
        <ProtectedRoute requiredRole="ADMIN">
          <ProtectedContent />
        </ProtectedRoute>,
      );

      const deniedContainer = container.querySelector(
        ".flex.h-screen.items-center.justify-center",
      );
      expect(deniedContainer).toBeInTheDocument();
    });
  });

  describe("Complex Children", () => {
    it("should render complex child components when authorized", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => true),
      });

      renderWithProviders(
        <ProtectedRoute>
          <div>
            <h1>Dashboard</h1>
            <p>Welcome back!</p>
            <button>Click me</button>
          </div>
        </ProtectedRoute>,
      );

      expect(screen.getByText("Dashboard")).toBeInTheDocument();
      expect(screen.getByText("Welcome back!")).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Click me" }),
      ).toBeInTheDocument();
    });

    it("should render multiple children when authorized", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => true),
      });

      renderWithProviders(
        <ProtectedRoute>
          <div>First Child</div>
          <div>Second Child</div>
        </ProtectedRoute>,
      );

      expect(screen.getByText("First Child")).toBeInTheDocument();
      expect(screen.getByText("Second Child")).toBeInTheDocument();
    });
  });

  describe("State Transitions", () => {
    it("should transition from loading to authenticated", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        hasRole: vi.fn(),
      });

      const { rerender } = renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();

      // Update mock to authenticated
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => true),
      });

      rerender(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
      expect(screen.getByText("Protected Content")).toBeInTheDocument();
    });

    it("should transition from loading to unauthenticated", () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        hasRole: vi.fn(),
      });

      const { rerender } = renderWithProviders(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.getByText("Loading...")).toBeInTheDocument();

      // Update mock to unauthenticated
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        hasRole: vi.fn(),
      });

      rerender(
        <ProtectedRoute>
          <ProtectedContent />
        </ProtectedRoute>,
      );

      expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
      expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
    });
  });
});
