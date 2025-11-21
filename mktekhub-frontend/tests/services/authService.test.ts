import { describe, it, expect, beforeEach, afterEach } from "vitest";
import MockAdapter from "axios-mock-adapter";
import api from "../../src/services/api";
import { authService } from "../../src/services/authService";
import type {
  LoginRequest,
  SignupRequest,
  AuthResponse,
} from "../../src/types";

describe("authService", () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(api);
    localStorage.clear();
  });

  afterEach(() => {
    mock.reset();
  });

  describe("login", () => {
    it("should successfully login with valid credentials", async () => {
      const loginRequest: LoginRequest = {
        username: "testuser",
        password: "password123",
      };

      const authResponse: AuthResponse = {
        token: "test-jwt-token",
        id: 1,
        username: "testuser",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_USER"],
      };

      mock.onPost("/auth/login", loginRequest).reply(200, authResponse);

      const result = await authService.login(loginRequest);

      expect(result).toEqual(authResponse);
      expect(result.token).toBe("test-jwt-token");
      expect(result.username).toBe("testuser");
    });

    it("should throw error on invalid credentials", async () => {
      const loginRequest: LoginRequest = {
        username: "wronguser",
        password: "wrongpass",
      };

      mock.onPost("/auth/login").reply(401, {
        message: "Invalid credentials",
      });

      await expect(authService.login(loginRequest)).rejects.toThrow();
    });

    it("should handle network errors", async () => {
      const loginRequest: LoginRequest = {
        username: "testuser",
        password: "password123",
      };

      mock.onPost("/auth/login").networkError();

      await expect(authService.login(loginRequest)).rejects.toThrow();
    });
  });

  describe("signup", () => {
    it("should successfully signup with valid data", async () => {
      const signupRequest: SignupRequest = {
        username: "newuser",
        email: "newuser@example.com",
        password: "password123",
        firstName: "New",
        lastName: "User",
      };

      const successMessage = "User registered successfully";

      mock
        .onPost("/auth/signup", signupRequest)
        .reply(201, { message: successMessage });

      const result = await authService.signup(signupRequest);

      expect(result).toBe(successMessage);
    });

    it("should throw error when username already exists", async () => {
      const signupRequest: SignupRequest = {
        username: "existinguser",
        email: "test@example.com",
        password: "password123",
        firstName: "Test",
        lastName: "User",
      };

      mock.onPost("/auth/signup").reply(409, {
        message: "Username already exists",
      });

      await expect(authService.signup(signupRequest)).rejects.toThrow();
    });

    it("should throw error on validation failure", async () => {
      const signupRequest: SignupRequest = {
        username: "a",
        email: "invalid-email",
        password: "123",
        firstName: "",
        lastName: "",
      };

      mock.onPost("/auth/signup").reply(400, {
        message: "Validation failed",
        validationErrors: {
          username: "Username must be at least 3 characters",
          email: "Invalid email format",
          password: "Password must be at least 6 characters",
        },
      });

      await expect(authService.signup(signupRequest)).rejects.toThrow();
    });
  });

  describe("logout", () => {
    it("should remove token and user from localStorage", () => {
      localStorage.setItem("token", "test-token");
      localStorage.setItem(
        "user",
        JSON.stringify({ username: "testuser", id: 1 }),
      );

      authService.logout();

      expect(localStorage.getItem("token")).toBeNull();
      expect(localStorage.getItem("user")).toBeNull();
    });

    it("should work even when localStorage is empty", () => {
      expect(() => authService.logout()).not.toThrow();
      expect(localStorage.getItem("token")).toBeNull();
      expect(localStorage.getItem("user")).toBeNull();
    });
  });

  describe("getCurrentUser", () => {
    it("should return user from localStorage when exists", () => {
      const user: AuthResponse = {
        token: "test-token",
        id: 1,
        username: "testuser",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_USER"],
      };

      localStorage.setItem("user", JSON.stringify(user));

      const result = authService.getCurrentUser();

      expect(result).toEqual(user);
    });

    it("should return null when no user in localStorage", () => {
      const result = authService.getCurrentUser();

      expect(result).toBeNull();
    });

    it("should return null when user data is invalid JSON", () => {
      localStorage.setItem("user", "invalid-json");

      expect(() => authService.getCurrentUser()).toThrow();
    });
  });

  describe("isAuthenticated", () => {
    it("should return true when token exists", () => {
      localStorage.setItem("token", "test-token");

      const result = authService.isAuthenticated();

      expect(result).toBe(true);
    });

    it("should return false when token does not exist", () => {
      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });

    it("should return false when token is empty string", () => {
      localStorage.setItem("token", "");

      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });
  });

  describe("hasRole", () => {
    it("should return true when user has the specified role", () => {
      const user: AuthResponse = {
        token: "test-token",
        id: 1,
        username: "testuser",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_USER", "ROLE_ADMIN"],
      };

      localStorage.setItem("user", JSON.stringify(user));

      expect(authService.hasRole("ROLE_ADMIN")).toBe(true);
      expect(authService.hasRole("ROLE_USER")).toBe(true);
    });

    it("should return false when user does not have the specified role", () => {
      const user: AuthResponse = {
        token: "test-token",
        id: 1,
        username: "testuser",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: ["ROLE_USER"],
      };

      localStorage.setItem("user", JSON.stringify(user));

      expect(authService.hasRole("ROLE_ADMIN")).toBe(false);
    });

    it("should return false when no user is logged in", () => {
      expect(authService.hasRole("ROLE_USER")).toBe(false);
    });

    it("should return false when user has no roles", () => {
      const user: AuthResponse = {
        token: "test-token",
        id: 1,
        username: "testuser",
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: [],
      };

      localStorage.setItem("user", JSON.stringify(user));

      expect(authService.hasRole("ROLE_USER")).toBe(false);
    });
  });
});
