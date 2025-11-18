import { afterEach, beforeEach, vi } from "vitest";
import "@testing-library/jest-dom";

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, "localStorage", {
  value: localStorageMock,
});

// Mock window.location
interface MockLocation {
  href: string;
  pathname: string;
  search: string;
  hash: string;
}

delete (window as unknown as { location: unknown }).location;
(window as unknown as { location: MockLocation }).location = {
  href: "",
  pathname: "",
  search: "",
  hash: "",
};

// Clear all mocks before each test
beforeEach(() => {
  localStorage.clear();
  vi.clearAllMocks();
});

// Clean up after each test
afterEach(() => {
  vi.resetAllMocks();
});
