import api from "./api";
import type { LoginRequest, SignupRequest, AuthResponse } from "../types";

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>("/auth/login", credentials);
    return response.data;
  },

  signup: async (data: SignupRequest): Promise<string> => {
    const response = await api.post<string>("/auth/signup", data);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  getCurrentUser: (): AuthResponse | null => {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem("token");
  },

  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser();
    return user?.roles.includes(role) || false;
  },
};
