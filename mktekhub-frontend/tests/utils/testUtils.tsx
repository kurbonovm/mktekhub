import { render, RenderOptions } from "@testing-library/react";
import { ReactElement, ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { ToastProvider } from "@/contexts/ToastContext";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi } from "vitest";
import type { Mock } from "vitest";

// Mock AuthContext value
export interface MockAuthContextValue {
  user: any;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: Mock;
  signup: Mock;
  logout: Mock;
  hasRole: Mock;
}

// Default mock auth context
export const mockAuthContext: MockAuthContextValue = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  signup: vi.fn(),
  logout: vi.fn(),
  hasRole: vi.fn(),
};

// Create a custom render function that includes providers
interface AllTheProvidersProps {
  children: ReactNode;
  authValue?: Partial<MockAuthContextValue>;
  initialRoute?: string;
}

function AllTheProviders({
  children,
  authValue = {},
  initialRoute = "/",
}: AllTheProvidersProps) {
  // Create a new QueryClient for each test to ensure isolation
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Disable retries in tests
      },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialRoute]}>
        <AuthProvider>
          <ToastProvider>{children}</ToastProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

interface CustomRenderOptions extends Omit<RenderOptions, "wrapper"> {
  authValue?: Partial<MockAuthContextValue>;
  initialRoute?: string;
}

// Custom render with providers
export const renderWithProviders = (
  ui: ReactElement,
  options?: CustomRenderOptions,
) => {
  const { authValue, initialRoute, ...renderOptions } = options || {};

  const Wrapper = ({ children }: { children: ReactNode }) => (
    <AllTheProviders authValue={authValue} initialRoute={initialRoute}>
      {children}
    </AllTheProviders>
  );

  return render(ui, { wrapper: Wrapper, ...renderOptions });
};

// Re-export everything from @testing-library/react
export * from "@testing-library/react";
export { renderWithProviders as render };
