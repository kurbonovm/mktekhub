import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom/vitest";
import { LoginPage } from "../../src/pages/LoginPage";
import { renderWithProviders } from "../utils/testUtils";
import * as AuthContext from "../../src/contexts/AuthContext";

// Mock the useNavigate hook
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("LoginPage", () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();

    // Mock useAuth hook
    vi.spyOn(AuthContext, "useAuth").mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: mockLogin,
      signup: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn(),
    });
  });

  describe("Rendering", () => {
    it("should render login form", () => {
      renderWithProviders(<LoginPage />);

      expect(screen.getByText("MKTekHub Inventory")).toBeInTheDocument();
      expect(screen.getByText("Sign in to your account")).toBeInTheDocument();
    });

    it("should render username input", () => {
      renderWithProviders(<LoginPage />);

      expect(
        screen.getByPlaceholderText("Enter your username"),
      ).toBeInTheDocument();
    });

    it("should render password input", () => {
      renderWithProviders(<LoginPage />);

      expect(
        screen.getByPlaceholderText("Enter your password"),
      ).toBeInTheDocument();
    });

    it("should render submit button", () => {
      renderWithProviders(<LoginPage />);

      expect(
        screen.getByRole("button", { name: /sign in/i }),
      ).toBeInTheDocument();
    });

    it("should render link to signup page", () => {
      renderWithProviders(<LoginPage />);

      const signupLink = screen.getByText("Sign up");
      expect(signupLink).toBeInTheDocument();
      expect(signupLink).toHaveAttribute("href", "/signup");
    });

    it("should have proper labels for accessibility", () => {
      renderWithProviders(<LoginPage />);

      expect(screen.getByLabelText("Username")).toBeInTheDocument();
      expect(screen.getByLabelText("Password")).toBeInTheDocument();
    });

    it("should have required attributes on inputs", () => {
      renderWithProviders(<LoginPage />);

      expect(screen.getByPlaceholderText("Enter your username")).toBeRequired();
      expect(screen.getByPlaceholderText("Enter your password")).toBeRequired();
    });

    it("should have password type on password input", () => {
      renderWithProviders(<LoginPage />);

      expect(
        screen.getByPlaceholderText("Enter your password"),
      ).toHaveAttribute("type", "password");
    });
  });

  describe("Form Interaction", () => {
    it("should update username on input change", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      const usernameInput = screen.getByPlaceholderText("Enter your username");
      await user.type(usernameInput, "testuser");

      expect(usernameInput).toHaveValue("testuser");
    });

    it("should update password on input change", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      const passwordInput = screen.getByPlaceholderText("Enter your password");
      await user.type(passwordInput, "password123");

      expect(passwordInput).toHaveValue("password123");
    });

    it("should clear inputs after typing and clearing", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      const usernameInput = screen.getByPlaceholderText("Enter your username");
      await user.type(usernameInput, "testuser");
      await user.clear(usernameInput);

      expect(usernameInput).toHaveValue("");
    });
  });

  describe("Form Submission - Success", () => {
    it("should call login with correct credentials on submit", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).toHaveBeenCalledWith({
        username: "testuser",
        password: "password123",
      });
    });

    it("should navigate to dashboard on successful login", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
      });

      // Wait for all state updates to complete
      await waitFor(() => {
        expect(screen.getByRole("button")).not.toBeDisabled();
      });
    });

    it("should show loading state during submission", async () => {
      const user = userEvent.setup();
      let resolveLogin: () => void;
      const loginPromise = new Promise<void>((resolve) => {
        resolveLogin = resolve;
      });
      mockLogin.mockReturnValue(loginPromise);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(screen.getByText("Signing in...")).toBeInTheDocument();
      expect(screen.getByRole("button")).toBeDisabled();

      resolveLogin!();
      await waitFor(() => {
        expect(screen.getByText("Sign in")).toBeInTheDocument();
      });
    });

    it("should not show error message on successful login", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });

      // Wait for all state updates to complete
      await waitFor(() => {
        expect(screen.getByRole("button")).not.toBeDisabled();
      });

      expect(screen.queryByText(/failed/i)).not.toBeInTheDocument();
    });
  });

  describe("Form Submission - Error Handling", () => {
    it("should display error message on login failure", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue({
        response: { data: { message: "Invalid credentials" } },
      });

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "wronguser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "wrongpass",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(screen.getByText("Invalid credentials")).toBeInTheDocument();
      });
    });

    it("should display default error message when no specific message is provided", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue(new Error("Network error"));

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(
          screen.getByText("Login failed. Please try again."),
        ).toBeInTheDocument();
      });
    });

    it("should not navigate on failed login", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue({
        response: { data: { message: "Invalid credentials" } },
      });

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(screen.getByText("Invalid credentials")).toBeInTheDocument();
      });

      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it("should clear previous error on new submission", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValueOnce({
        response: { data: { message: "First error" } },
      });

      renderWithProviders(<LoginPage />);

      // First failed attempt
      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "user1",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "pass1",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(screen.getByText("First error")).toBeInTheDocument();
      });

      // Second attempt with success
      mockLogin.mockResolvedValue(undefined);
      await user.clear(screen.getByPlaceholderText("Enter your username"));
      await user.clear(screen.getByPlaceholderText("Enter your password"));
      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "user2",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "pass2",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });

      // Wait for all state updates to complete
      await waitFor(() => {
        expect(screen.getByRole("button")).not.toBeDisabled();
      });

      expect(screen.queryByText("First error")).not.toBeInTheDocument();
    });

    it("should reset loading state after error", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue({
        response: { data: { message: "Error" } },
      });

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(screen.getByText("Error")).toBeInTheDocument();
      });

      expect(screen.getByText("Sign in")).toBeInTheDocument();
      expect(screen.getByRole("button")).not.toBeDisabled();
    });
  });

  describe("Form Validation", () => {
    it("should prevent submission with empty username", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).not.toHaveBeenCalled();
    });

    it("should prevent submission with empty password", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).not.toHaveBeenCalled();
    });

    it("should prevent submission with both fields empty", async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginPage />);

      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).not.toHaveBeenCalled();
    });
  });

  describe("Button States", () => {
    it("should disable button while loading", async () => {
      const user = userEvent.setup();
      let resolveLogin: () => void;
      const loginPromise = new Promise<void>((resolve) => {
        resolveLogin = resolve;
      });
      mockLogin.mockReturnValue(loginPromise);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(screen.getByRole("button")).toBeDisabled();

      resolveLogin!();

      // Wait for the promise to resolve and state updates to complete
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });
    });

    it("should change button text while loading", async () => {
      const user = userEvent.setup();
      let resolveLogin: () => void;
      const loginPromise = new Promise<void>((resolve) => {
        resolveLogin = resolve;
      });
      mockLogin.mockReturnValue(loginPromise);

      renderWithProviders(<LoginPage />);

      expect(screen.getByText("Sign in")).toBeInTheDocument();

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(screen.getByText("Signing in...")).toBeInTheDocument();

      resolveLogin!();

      // Wait for the promise to resolve and state updates to complete
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });
    });

    it("should re-enable button after successful login", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalled();
      });

      // Wait for all state updates to complete
      await waitFor(() => {
        expect(screen.getByRole("button")).not.toBeDisabled();
      });
    });
  });

  describe("Error Message Display", () => {
    it("should show error with red background", async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue({
        response: { data: { message: "Error message" } },
      });

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      await waitFor(() => {
        const errorElement = screen.getByText("Error message");
        expect(errorElement).toBeInTheDocument();
        expect(errorElement.className).toMatch(/text-red-800/);
      });
    });

    it("should not show error message initially", () => {
      renderWithProviders(<LoginPage />);

      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle very long username", async () => {
      const user = userEvent.setup();
      const longUsername = "a".repeat(100);
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        longUsername,
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).toHaveBeenCalledWith({
        username: longUsername,
        password: "password123",
      });
    });

    it("should handle special characters in credentials", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "user@example.com",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "p@ss$w0rd!",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).toHaveBeenCalledWith({
        username: "user@example.com",
        password: "p@ss$w0rd!",
      });
    });

    it("should handle whitespace in inputs", async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);

      renderWithProviders(<LoginPage />);

      await user.type(
        screen.getByPlaceholderText("Enter your username"),
        "  testuser  ",
      );
      await user.type(
        screen.getByPlaceholderText("Enter your password"),
        "  pass  ",
      );
      await user.click(screen.getByRole("button", { name: /sign in/i }));

      expect(mockLogin).toHaveBeenCalledWith({
        username: "  testuser  ",
        password: "  pass  ",
      });
    });
  });

  describe("Navigation", () => {
    it("should have working link to signup page", () => {
      renderWithProviders(<LoginPage />);

      const signupLink = screen.getByText("Sign up");
      expect(signupLink).toHaveAttribute("href", "/signup");
    });

    it("should display signup prompt text", () => {
      renderWithProviders(<LoginPage />);

      expect(screen.getByText("Don't have an account?")).toBeInTheDocument();
    });
  });
});
