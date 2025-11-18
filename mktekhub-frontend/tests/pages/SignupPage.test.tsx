import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import userEvent from "@testing-library/user-event";
import { SignupPage } from "../../src/pages/SignupPage";
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

describe("SignupPage", () => {
  const mockSignup = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();

    // Mock useAuth hook
    vi.spyOn(AuthContext, "useAuth").mockReturnValue({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      signup: mockSignup,
      logout: vi.fn(),
      hasRole: vi.fn(),
    });
  });

  describe("Rendering", () => {
    it("should render signup form", () => {
      renderWithProviders(<SignupPage />);

      expect(screen.getByText("MKTekHub Inventory")).toBeInTheDocument();
      expect(screen.getByText("Create your account")).toBeInTheDocument();
    });

    it("should render all form fields", () => {
      renderWithProviders(<SignupPage />);

      expect(screen.getByLabelText("Username")).toBeInTheDocument();
      expect(screen.getByLabelText("Email")).toBeInTheDocument();
      expect(screen.getByLabelText("First Name")).toBeInTheDocument();
      expect(screen.getByLabelText("Last Name")).toBeInTheDocument();
      expect(screen.getByLabelText("Password")).toBeInTheDocument();
      expect(screen.getByLabelText("Confirm Password")).toBeInTheDocument();
    });

    it("should render submit button", () => {
      renderWithProviders(<SignupPage />);

      expect(
        screen.getByRole("button", { name: /sign up/i }),
      ).toBeInTheDocument();
    });

    it("should render link to login page", () => {
      renderWithProviders(<SignupPage />);

      const loginLink = screen.getByText("Sign in");
      expect(loginLink).toBeInTheDocument();
      expect(loginLink).toHaveAttribute("href", "/login");
    });

    it("should have required attributes on all inputs", () => {
      renderWithProviders(<SignupPage />);

      expect(screen.getByPlaceholderText("Enter username")).toBeRequired();
      expect(screen.getByPlaceholderText("Enter email")).toBeRequired();
      expect(screen.getByPlaceholderText("Enter first name")).toBeRequired();
      expect(screen.getByPlaceholderText("Enter last name")).toBeRequired();
      expect(screen.getByPlaceholderText("Enter password")).toBeRequired();
      expect(screen.getByPlaceholderText("Confirm password")).toBeRequired();
    });

    it("should have correct input types", () => {
      renderWithProviders(<SignupPage />);

      expect(screen.getByPlaceholderText("Enter email")).toHaveAttribute(
        "type",
        "email",
      );
      expect(screen.getByPlaceholderText("Enter password")).toHaveAttribute(
        "type",
        "password",
      );
      expect(screen.getByPlaceholderText("Confirm password")).toHaveAttribute(
        "type",
        "password",
      );
    });
  });

  describe("Form Interaction", () => {
    it("should update username on input change", async () => {
      const user = userEvent.setup();
      renderWithProviders(<SignupPage />);

      const usernameInput = screen.getByPlaceholderText("Enter username");
      await user.type(usernameInput, "newuser");

      expect(usernameInput).toHaveValue("newuser");
    });

    it("should update email on input change", async () => {
      const user = userEvent.setup();
      renderWithProviders(<SignupPage />);

      const emailInput = screen.getByPlaceholderText("Enter email");
      await user.type(emailInput, "user@example.com");

      expect(emailInput).toHaveValue("user@example.com");
    });

    it("should update all form fields", async () => {
      const user = userEvent.setup();
      renderWithProviders(<SignupPage />);

      await user.type(
        screen.getByPlaceholderText("Enter username"),
        "testuser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "test@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "John");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Doe");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );

      expect(screen.getByPlaceholderText("Enter username")).toHaveValue(
        "testuser",
      );
      expect(screen.getByPlaceholderText("Enter email")).toHaveValue(
        "test@example.com",
      );
      expect(screen.getByPlaceholderText("Enter first name")).toHaveValue(
        "John",
      );
      expect(screen.getByPlaceholderText("Enter last name")).toHaveValue("Doe");
      expect(screen.getByPlaceholderText("Enter password")).toHaveValue(
        "password123",
      );
      expect(screen.getByPlaceholderText("Confirm password")).toHaveValue(
        "password123",
      );
    });
  });

  describe("Form Submission - Success", () => {
    it("should call signup with correct data on submit", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("User created successfully");

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));
      await waitFor(() => {
        expect(mockSignup).toHaveBeenCalledWith({
          username: "newuser",
          email: "new@example.com",
          firstName: "Jane",
          lastName: "Smith",
          password: "password123",
        });
      });
    });

    it("should show success message on successful signup", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("User created successfully");

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(
          screen.getByText("User created successfully"),
        ).toBeInTheDocument();
      });
    });

    it("should navigate to login after 2 seconds on success", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("User created successfully");

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(
          screen.getByText("User created successfully"),
        ).toBeInTheDocument();
      });

      // Wait for navigation to be called (2 seconds in component)
      await waitFor(
        () => {
          expect(mockNavigate).toHaveBeenCalledWith("/login");
        },
        { timeout: 3000 },
      );
    }, 10000);

    it("should show loading state during submission", async () => {
      const user = userEvent.setup();
      let resolveSignup: (value: string) => void;
      const signupPromise = new Promise<string>((resolve) => {
        resolveSignup = resolve;
      });
      mockSignup.mockReturnValue(signupPromise);

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Creating account...")).toBeInTheDocument();
        expect(screen.getByRole("button")).toBeDisabled();
      });

      resolveSignup!("Success");
      await waitFor(() => {
        expect(screen.getByText("Sign up")).toBeInTheDocument();
      });
    });
  });

  describe("Form Validation - Password Mismatch", () => {
    it("should show error when passwords do not match", async () => {
      const user = userEvent.setup();
      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password456",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
      });
      expect(mockSignup).not.toHaveBeenCalled();
    });

    it("should not submit when passwords do not match", async () => {
      const user = userEvent.setup();
      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(screen.getByPlaceholderText("Enter password"), "pass1");
      await user.type(screen.getByPlaceholderText("Confirm password"), "pass2");
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      expect(mockSignup).not.toHaveBeenCalled();
    });
  });

  describe("Form Submission - Error Handling", () => {
    it("should display error message on signup failure", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValue({
        response: { data: { message: "Username already exists" } },
      });

      renderWithProviders(<SignupPage />);

      await user.type(
        screen.getByPlaceholderText("Enter username"),
        "existinguser",
      );
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "user@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "John");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Doe");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Username already exists")).toBeInTheDocument();
      });
    });

    it("should display default error message when no specific message is provided", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValue(new Error("Network error"));

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(
          screen.getByText("Signup failed. Please try again."),
        ).toBeInTheDocument();
      });
    });

    it("should not navigate on failed signup", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValue({
        response: { data: { message: "Error" } },
      });

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Error")).toBeInTheDocument();
      });

      // Navigation should never be called on failure
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it("should reset loading state after error", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValue({
        response: { data: { message: "Error" } },
      });

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Error")).toBeInTheDocument();
      });

      expect(screen.getByText("Sign up")).toBeInTheDocument();
      expect(screen.getByRole("button")).not.toBeDisabled();
    });
  });

  describe("Error and Success Message Clearing", () => {
    it("should clear previous error on new submission", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValueOnce({
        response: { data: { message: "First error" } },
      });

      renderWithProviders(<SignupPage />);

      // First failed attempt
      await user.type(screen.getByPlaceholderText("Enter username"), "user1");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "user@test.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "John");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Doe");
      await user.type(screen.getByPlaceholderText("Enter password"), "pass123");
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "pass123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("First error")).toBeInTheDocument();
      });

      // Second attempt with success
      mockSignup.mockResolvedValue("Success");
      await user.clear(screen.getByPlaceholderText("Enter username"));
      await user.type(screen.getByPlaceholderText("Enter username"), "user2");
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Success")).toBeInTheDocument();
      });
      expect(screen.queryByText("First error")).not.toBeInTheDocument();
    });

    it("should clear success message on new submission", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValueOnce("First success");

      renderWithProviders(<SignupPage />);

      // First success
      await user.type(screen.getByPlaceholderText("Enter username"), "user1");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "user@test.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "John");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Doe");
      await user.type(screen.getByPlaceholderText("Enter password"), "pass123");
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "pass123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("First success")).toBeInTheDocument();
      });

      // Second submission with error
      mockSignup.mockRejectedValue({
        response: { data: { message: "Error" } },
      });
      await user.clear(screen.getByPlaceholderText("Enter password"));
      await user.clear(screen.getByPlaceholderText("Confirm password"));
      await user.type(screen.getByPlaceholderText("Enter password"), "wrong");
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "different",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
      });
      expect(screen.queryByText("First success")).not.toBeInTheDocument();
    });
  });

  describe("Button States", () => {
    it("should disable button while loading", async () => {
      const user = userEvent.setup();
      let resolveSignup: (value: string) => void;
      const signupPromise = new Promise<string>((resolve) => {
        resolveSignup = resolve;
      });
      mockSignup.mockReturnValue(signupPromise);

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByRole("button")).toBeDisabled();
      });

      resolveSignup!("Success");
    });

    it("should change button text while loading", async () => {
      const user = userEvent.setup();
      let resolveSignup: (value: string) => void;
      const signupPromise = new Promise<string>((resolve) => {
        resolveSignup = resolve;
      });
      mockSignup.mockReturnValue(signupPromise);

      renderWithProviders(<SignupPage />);

      expect(screen.getByText("Sign up")).toBeInTheDocument();

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(screen.getByText("Creating account...")).toBeInTheDocument();
      });

      resolveSignup!("Success");
    });
  });

  describe("Navigation", () => {
    it("should have working link to login page", () => {
      renderWithProviders(<SignupPage />);

      const loginLink = screen.getByText("Sign in");
      expect(loginLink).toHaveAttribute("href", "/login");
    });

    it("should display login prompt text", () => {
      renderWithProviders(<SignupPage />);

      expect(screen.getByText("Already have an account?")).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle special characters in inputs", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("Success");

      renderWithProviders(<SignupPage />);

      await user.type(
        screen.getByPlaceholderText("Enter username"),
        "user@123",
      );
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "test+tag@example.com",
      );
      await user.type(
        screen.getByPlaceholderText("Enter first name"),
        "Mary-Jane",
      );
      await user.type(
        screen.getByPlaceholderText("Enter last name"),
        "O'Brien",
      );
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "P@ssw0rd!",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "P@ssw0rd!",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        expect(mockSignup).toHaveBeenCalledWith({
          username: "user@123",
          email: "test+tag@example.com",
          firstName: "Mary-Jane",
          lastName: "O'Brien",
          password: "P@ssw0rd!",
        });
      });
    });

    it("should not include confirmPassword in signup call", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("Success");

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        const callArgs = mockSignup.mock.calls[0][0];
        expect(callArgs).not.toHaveProperty("confirmPassword");
      });
    });
  });

  describe("Message Styling", () => {
    it("should show error with red background", async () => {
      const user = userEvent.setup();
      mockSignup.mockRejectedValue({
        response: { data: { message: "Error message" } },
      });

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        const errorElement = screen.getByText("Error message");
        expect(errorElement).toBeInTheDocument();
        expect(errorElement.className).toMatch(/text-red-800/);
      });
    });

    it("should show success with green background", async () => {
      const user = userEvent.setup();
      mockSignup.mockResolvedValue("Success message");

      renderWithProviders(<SignupPage />);

      await user.type(screen.getByPlaceholderText("Enter username"), "newuser");
      await user.type(
        screen.getByPlaceholderText("Enter email"),
        "new@example.com",
      );
      await user.type(screen.getByPlaceholderText("Enter first name"), "Jane");
      await user.type(screen.getByPlaceholderText("Enter last name"), "Smith");
      await user.type(
        screen.getByPlaceholderText("Enter password"),
        "password123",
      );
      await user.type(
        screen.getByPlaceholderText("Confirm password"),
        "password123",
      );
      await user.click(screen.getByRole("button", { name: /sign up/i }));

      await waitFor(() => {
        const successElement = screen.getByText("Success message");
        expect(successElement).toBeInTheDocument();
        expect(successElement.className).toMatch(/text-green-800/);
      });
    });
  });
});
