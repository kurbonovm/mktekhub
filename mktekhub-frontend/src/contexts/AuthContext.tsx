import React, { createContext, useContext, useState, useEffect } from "react";
import type { AuthResponse, LoginRequest, SignupRequest } from "../types";
import { authService } from "../services/authService";

interface AuthContextType {
  user: AuthResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  signup: (data: SignupRequest) => Promise<string>;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on mount
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      setUser(currentUser);
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authService.login(credentials);
    localStorage.setItem("token", response.token);
    localStorage.setItem("user", JSON.stringify(response));
    setUser(response);
  };

  const signup = async (data: SignupRequest): Promise<string> => {
    const message = await authService.signup(data);
    return message;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const hasRole = (role: string): boolean => {
    return user?.roles.includes(role) || false;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        signup,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};
