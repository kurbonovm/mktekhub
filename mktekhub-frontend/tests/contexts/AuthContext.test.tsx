import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { AuthProvider, useAuth } from "../../src/contexts/AuthContext";
import { authService } from "../../src/services/authService";
import type { AuthResponse } from "../../src/types";

// Mock the authService
vi.mock("../../src/services/authService", () => ({
  authService: {
    login: vi.fn(),
    signup: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}));

describe("AuthContext", () => {
  const mockUser: AuthResponse = {
    id: 1,
    username: "testuser",
    email: "test@example.com",
    roles: ["ADMIN", "MANAGER"],
    token: "mock-token-123",
  };

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe("AuthProvider initialization", () => {
    it("should provide auth context to children", () => {
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      expect(result.current).toBeDefined();
      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });

    it("should load user from authService on mount", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.isAuthenticated).toBe(true);
    });

    it("should set isLoading to false when no user is found", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });

    it("should eventually set isLoading to false", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Wait for loading to complete
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      // Ensure it stays false
      expect(result.current.isLoading).toBe(false);
    });
  });

  describe("useAuth hook", () => {
    it("should throw error when used outside AuthProvider", () => {
      // Suppress console.error for this test
      const consoleError = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      expect(() => {
        renderHook(() => useAuth());
      }).toThrow("useAuth must be used within AuthProvider");

      consoleError.mockRestore();
    });
  });

  describe("login", () => {
    it("should login user and update state", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.login).mockResolvedValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await act(async () => {
        await result.current.login({
          username: "testuser",
          password: "password123",
        });
      });

      expect(authService.login).toHaveBeenCalledWith({
        username: "testuser",
        password: "password123",
      });
      expect(result.current.user).toEqual(mockUser);
      expect(result.current.isAuthenticated).toBe(true);
      expect(localStorage.getItem("token")).toBe("mock-token-123");
      expect(localStorage.getItem("user")).toBe(JSON.stringify(mockUser));
    });

    it("should store token and user in localStorage", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.login).mockResolvedValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await act(async () => {
        await result.current.login({
          username: "testuser",
          password: "password123",
        });
      });

      expect(localStorage.getItem("token")).toBe(mockUser.token);
      const storedUser = JSON.parse(localStorage.getItem("user") || "{}");
      expect(storedUser).toEqual(mockUser);
    });

    it("should handle login errors", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.login).mockRejectedValue(
        new Error("Invalid credentials"),
      );

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await expect(
        result.current.login({
          username: "wronguser",
          password: "wrongpass",
        }),
      ).rejects.toThrow("Invalid credentials");

      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe("signup", () => {
    it("should call authService.signup with correct data", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.signup).mockResolvedValue(
        "User created successfully",
      );

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      const signupData = {
        username: "newuser",
        email: "new@example.com",
        password: "password123",
        roles: ["USER"],
      };

      let message: string = "";
      await act(async () => {
        message = await result.current.signup(signupData);
      });

      expect(authService.signup).toHaveBeenCalledWith(signupData);
      expect(message).toBe("User created successfully");
    });

    it("should return success message from service", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.signup).mockResolvedValue(
        "Registration successful",
      );

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      let message: string = "";
      await act(async () => {
        message = await result.current.signup({
          username: "user",
          email: "user@test.com",
          password: "pass",
          roles: ["USER"],
        });
      });

      expect(message).toBe("Registration successful");
    });

    it("should handle signup errors", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.signup).mockRejectedValue(
        new Error("Username already exists"),
      );

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await expect(
        result.current.signup({
          username: "existinguser",
          email: "test@test.com",
          password: "pass",
          roles: ["USER"],
        }),
      ).rejects.toThrow("Username already exists");
    });

    it("should not update user state after signup", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.signup).mockResolvedValue("Success");

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await act(async () => {
        await result.current.signup({
          username: "newuser",
          email: "new@test.com",
          password: "pass",
          roles: ["USER"],
        });
      });

      // User should still be null - signup doesn't auto-login
      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe("logout", () => {
    it("should logout user and clear state", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);
      vi.mocked(authService.logout).mockImplementation(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
      });

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      act(() => {
        result.current.logout();
      });

      expect(authService.logout).toHaveBeenCalled();
      expect(result.current.user).toBe(null);
      expect(result.current.isAuthenticated).toBe(false);
    });

    it("should clear localStorage on logout", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);
      vi.mocked(authService.logout).mockImplementation(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
      });

      // Set initial localStorage
      localStorage.setItem("token", "some-token");
      localStorage.setItem("user", JSON.stringify(mockUser));

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      act(() => {
        result.current.logout();
      });

      expect(localStorage.getItem("token")).toBe(null);
      expect(localStorage.getItem("user")).toBe(null);
    });

    it("should handle logout when user is not logged in", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      act(() => {
        result.current.logout();
      });

      expect(authService.logout).toHaveBeenCalled();
      expect(result.current.user).toBe(null);
    });
  });

  describe("hasRole", () => {
    it("should return true when user has the role", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.hasRole("ADMIN")).toBe(true);
      expect(result.current.hasRole("MANAGER")).toBe(true);
    });

    it("should return false when user does not have the role", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.hasRole("USER")).toBe(false);
      expect(result.current.hasRole("EMPLOYEE")).toBe(false);
    });

    it("should return false when user is not logged in", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.hasRole("ADMIN")).toBe(false);
      expect(result.current.hasRole("USER")).toBe(false);
    });

    it("should handle case-sensitive role checking", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.hasRole("ADMIN")).toBe(true);
      expect(result.current.hasRole("admin")).toBe(false);
    });
  });

  describe("isAuthenticated", () => {
    it("should return true when user is logged in", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.isAuthenticated).toBe(true);
    });

    it("should return false when user is not logged in", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(false);
    });

    it("should update when user logs in", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(null);
      vi.mocked(authService.login).mockResolvedValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(false);

      await act(async () => {
        await result.current.login({
          username: "test",
          password: "pass",
        });
      });

      expect(result.current.isAuthenticated).toBe(true);
    });

    it("should update when user logs out", async () => {
      vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.user).toEqual(mockUser);
      });

      expect(result.current.isAuthenticated).toBe(true);

      act(() => {
        result.current.logout();
      });

      expect(result.current.isAuthenticated).toBe(false);
    });
  });
});
